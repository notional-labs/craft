// create an express app
const express = require("express")
const app = express()

// use the express-static middleware
app.use(express.static(__dirname))

// start the server listening for requests
app.listen(process.env.PORT || 3000, 
	() => console.log("Server is running..."));