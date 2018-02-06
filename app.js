//api key: 31186b9d7d394a0ea814cb983f33844e
//api key clairifai: cec5cee01cab4ca6862b704d81e3dd8f
//api key Google: AIzaSyDV8sw03H_AT0BG4BIlCtIq0-LzBEHbT3g
var     express               = require("express"),
        app                   = express(), 
        bodyparser            = require("body-parser"),
        request               = require("request"),
        mongoose              = require("mongoose"), 
        Clarifai              = require('clarifai'),
        firebase              = require("firebase"),
        base64Img             = require('base64-img'),
        moment                = require('moment'); 
        
app.set("view engine", "ejs"); 
app.use(express.static("public"));
app.use(bodyparser.urlencoded({extended:true}));
var fs = require('fs');

var   calories, 
      fat,  
      sugar,
      protein,
      sodium,
      time;
//================================================================================================
//             mongoose.connect("mongodb://localhost/foodapp"); 
                    
//================================================================================================

mongoose.connect("mongodb://localhost/foodapp"); 
mongodb://kartik34:kartik@ds225078.mlab.com:25078/hackthehammer
var foodSchema = new mongoose.Schema({
    name: String, 
    calories: Number, 
    fat: Number, 
    sugar: Number, 
    protein: Number,
    sodium: Number, 
    time: String

});

var food = mongoose.model("Food", foodSchema);


//================================================================================================
//                                   API KEYS AND SETUP  
//================================================================================================

var NutritionixClient = require('nutritionix');
var nutritionix = new NutritionixClient({
    appId: '6d1f92ef',
    appKey: 'ab03b6d89bbf9a12b5b787328b6bf1ac'
});
var vision = new Clarifai.App({
 apiKey: 'aa68a28c7d524a8ebeb9a8ac4d0bba92'
});
//================================================================================================
//                                   FIREBASE 
//================================================================================================

var admin = require("firebase-admin");

var serviceAccount = require("./key.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    storageBucket: "hack-the-hammer.appspot.com",
    databaseURL: "https://hack-the-hammer.firebaseio.com/"
});

var bucket = admin.storage().bucket();


//================================================================================================
//                               CLARIFAI API
//================================================================================================

app.get("/index3", function(req, res) {
    res.render("index3")
})

// app.get("/visionTest", function(req,res){
    
//     res.render("visiontest"); 
// })
var name; 
time = moment().subtract(5, 'hours').format('H:mm:ss')
function round(value, precision) {
    var multiplier = Math.pow(10, precision || 0);
    return Math.round(value * multiplier) / multiplier;
}
app.get("/index", function(req,res){
    
    bucket.getFiles()
    .then(results => {
      const files = results[0];
    //   console.log('Files:');
     files[0].download({ destination: './image.txt'}, function(err) {
            if(err){
                console.log(err)
            }
            files[0].delete(function(err){
                if(err){
                    console.log(err)
                }
            })
            fs.readFile("./image.txt", function(err, original_data){
              if(err){
                 return;
            }
              else{
                 // this variable contains the correctly encoded image. Just use it as it is in the Clarifai API
                var base64Image = original_data.toString('base64');
                vision.models.predict(Clarifai.FOOD_MODEL, {base64: base64Image}).then(

                function(response) {
                  name = response.outputs[0].data.concepts[0].name
                  console.log(response.outputs[0].data.concepts[0].name);
                  var ingredients = [
                         name
                     ];
                     nutritionix.natural(ingredients.join('\n')).then(successHandler)
                    function successHandler(x){
                        console.log(x.total.nutrients); 
                        
                        x.total.nutrients.forEach(function(fact){
                            
                            if(fact.name == 'Protein'){
                                console.log(fact.name)
                                protein = fact.value; 
                            }
                            else if(fact.name == 'Energy'){
                                calories = fact.value; 
                            }
                            else if(fact.name == 'Sugars, total'){
                                sugar = fact.value; 
                            }
                            else if(fact.name == 'Sodium, Na'){
                                sodium = fact.value; 
                            }
                            else if(fact.name == 'Total lipid (fat)'){
                                fat = fact.value; 
                            }
                             
                        })
                       
                        
                        food.create({
                            name: name,
                            protein: protein, 
                            calories: calories,
                            sugar: sugar, 
                            sodium: sodium, 
                            fat: fat,
                            time: time 
                            
                        }, function(err,body){
                            if(err){
                                console.log("An error occured");
                            }else{
                            admin.database().ref("/").update({"name":name})
                            admin.database().ref("/").update({"calories":round(calories, 1)+" calories"})
                            admin.database().ref("/").update({"sugar": round(sugar, 1)+" grams"})
                            admin.database().ref("/").update({"fat": round(fat, 1)+" grams"})
                            admin.database().ref("/").update({"sodium": round(sodium, 1)+" milligrams"})
                            admin.database().ref("/").update({"protein": round(protein, 1)+" grams"})

                            
                             food.find({}, function(err, body){
                                if(err){
                                    console.log(err)
                                }
                                res.render("index", {body: body})
                             })
                        }
                        })
                    } 
                  }
                  
              );
             }
            });
            
        });
     
    })
    .catch(err => {
      console.error('ERROR:', err);
    });
    
   
})

//================================================================================================
//                                NUTRITION API
//================================================================================================

app.get("/test", function(req,res){
    
    food.remove({}); 
    
    
})

// app.post("/test", function(req,res){
    
//     var ingredients = [
//         req.body.food 
//     ];

//     // ensure you are passing a string with queries delimited by new lines.
    
//     function errorHandler(err){
//         console.log(err);
//     }
    
// })






app.listen(process.env.PORT, process.env.IP, function(){
    console.log("server is running");
});
