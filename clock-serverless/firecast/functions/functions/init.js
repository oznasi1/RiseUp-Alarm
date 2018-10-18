
var admin = require("firebase-admin");

// Fetch the service account key JSON file contents
//var serviceAccount = require('../../../ClockPlus-master/alarmclock-22904-firebase-adminsdk-5h69c-6d28a8a884.json');

// Initialize the app with a service account, granting admin privileges

var credentials = require("./credentials.json");

admin.initializeApp({
  credential: admin.credential.cert(credentials),
  databaseURL: "https://alarmclock-22904.firebaseio.com"
});

const db = admin.database();

module.exports = db;