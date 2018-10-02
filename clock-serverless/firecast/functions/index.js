const functions = require("firebase-functions");

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//

var admin = require("firebase-admin");

// Fetch the service account key JSON file contents
//var serviceAccount = require('../../../ClockPlus-master/alarmclock-22904-firebase-adminsdk-5h69c-6d28a8a884.json');

// Initialize the app with a service account, granting admin privileges
// admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount),
//     databaseURL: "https://alarmclock-22904.firebaseio.com"
// });

admin.initializeApp({
  credential: admin.credential.cert({
    projectId: "alarmclock-22904",
    clientEmail:
      "firebase-adminsdk-5h69c@alarmclock-22904.iam.gserviceaccount.com",
    privateKey:
      "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCn6oNZu+e8LagL\nRd3nezn79QgUUyhj9i5X0B7T2e35ikD81Dry2WzZzZA3HNbYbflodrXRXFWJol94\niK+mSafe5moCGt8BfRL1kNGEetx7zboXqgS9sftewKGepmD9cgTGtJDYGNbCaaoV\nlue4maL0AxrMG3ENUoUuFgoA0VAGKbnu+0cBZrKpzXanqq3pwvN2x25F38rFN4Hw\nTXJXIuoWapd+Jkm2YhwC9Hxx4uhKxMA238OBi1fmuL2np8gWI4IQstZrq+F8LpWP\n2U+w7elnBkE6zYCnXp3RkNgjo0zYtfAJav7Razd10NRTgJRGM+Uayzo0ca+82BPx\n3bqp15c/AgMBAAECggEAFitHmTbOQxFyEL7yK6ggwSuaG/jYqc3sU2qmT0kD+sCu\nkNLCT6K2FNFne2dC1EM8vPaDTbdXkbrOyP6e1iD+WVbpYwrCK5OkR6iNiOShrHS+\nX3ZydssDSYC8NdJ4lq2RhNx9DRLGc9d4fa41PLGICphIVu8+j0g4ThvZyT7ZRl7Q\nFyIBBMzoEI7haIt1A1dxG8MuVQTEdPb8EottZzddetSMCB1stmBfMrFnbv4q5+CL\nFK2s341kisj5WOTbegd5EBsjZsaW2K8DakxjKxu4R4QARo9UjXbF6ovV8sun8Cwi\nsEQ71zRwLFcv3zSH38w1Ay3oogMhigViS43mDTnWSQKBgQDbwXcIXt27leF0RJ57\nQZ3Q48g+ArlnD/IENpH7O3XvONrKVd8IgpRzTCTS/gag3t4+sgpGYcim8uAtHvhI\nO33VI+c9bLwG3pu03P5T1yVF4ErU1hzTRYfLKAdPTQlqJ9p8l50bX5yuRN+OB6ZV\nOyHRRn7BRpGOD5m/k4SMIPowlwKBgQDDnEWmNZk6IyzwSC/RlqGIymeGDuWM+Y8/\n/eKy7azPuNvFB+0q/RCzWUEuza7dwDWm5TDOlavCNg+QBSLvrkxKfL55xP3M8DbB\nl/tLKIhKbvNoaX6v36H4NpmKiR/7GuTsQB2QBr0VUihnQluoHHNR9wk2WK0XvvBM\nvKpZn8h7mQKBgDmCjOMs6HxSAhmKZ6s8BdAH/Hx1/A1DuANwQI3uBrQfPerbxC2W\n9sChubVZ82QgTys7LsMyje9a+l30j+dfYlPoPHjvvtlRYpBXkVthn4iE5TCEBDEq\nLhp1lEwljgQw/9XBW8UxJXDZ52NlDxuZCQWanEt6a1cxL/xZ38NIUhFrAoGAT9FW\ngkngql5sGef6yzUB2QVuVL+DX5D9qQj6FjTxjRlLZAyl9CKrS+Ohu21ma0nhGqbu\n0X01Rtw0YXoXFWIz8zkPqDR/s75ZOYnAmpyBcI3xeKuDbFR+jQAKiwMFL7t7qgNH\njNuTED4kCm6DZAen7sw00B5DEytwdn+Nq2CNuBECgYEAixyqLuuGNcorGqTHT1I5\nL1XKSVbiun7iBMPlX9axdyY3T8RjlwQhLsSEWsDTVuPUUKX99OmDAAcsvUwUQbUN\nQRFfIAXUEucPDnUNrDmaGESd2qbbIYWzCRuHQyeoIe1f4ks3PYDRF9wjqlDPjIf5\nCYDMphtU+/4WZli/p8KGdII=\n-----END PRIVATE KEY-----\n"
  }),
  databaseURL: "https://alarmclock-22904.firebaseio.com"
});

function getRandomInt(max) {
  return Math.floor(Math.random() * Math.floor(max));
}

const rootGroupId = "-1";
const db = admin.database();

async function getNewSongUrl(user) {
  let lastLikedSong = await getLastLikedSong(user);
  let nextGroup = await getNextGroup(lastLikedSong.songId);
  let size = Object.keys(nextGroup).length;
  let randomIndex = getRandomInt(size - 1);
  let nextSongId = Object.keys(nextGroup)[randomIndex];
  let nextSongUrl = nextGroup[nextSongId].url;//TODO: remove unliked songs

  console.log(); //TEST
  return {
      url: nextSongUrl,
      songId: nextSongId
  }
}

async function getNextGroup(lastSongId) {
  //first tries to get the group that the current song brought to the db
  //(song's children in tree). we assume it's not a leaf.
  let nextGroupID = lastSongId;
  let matchGroupQuery = db
    .ref("songs")
    .orderByChild("groupId")
    .equalTo(nextGroupID);

  try {
    let snapshot = await matchGroupQuery.once("value");
    let arrGroup = snapshot.val();
    //checkig if it's a leaf-no children
    if (arrGroup == null) {
      throw new Error("Leaf Exception");
    } else {
      return arrGroup;
    }
  } catch (err) {
      let prevGroup = await getSongGroupId(lastSongId);
      let res = await getNextGroup(prevGroup);
      return res;
  }
}

async function getSong(songId) {
  let songQuery = db
    .ref("songs")
    .orderByKey()
    .equalTo(songId);

  let res;
  try {
    let snapshot = await songQuery.once("value");
    res = snapshot.val()[songId];

    return res;
    
  } catch (err) {
    console.error(err);
  }
}

async function getSongGroupId(songId) {
  let res = await getSong(songId);
    return res.groupId;
}

async function getLastLikedSong(user) {
  var likeSongsQuery = db
    .ref("/users")
    .child(user)
    .child("history")
    .orderByChild("isLiked")
    .equalTo("1");
  try {
    let snapshot = await likeSongsQuery.once("value");
    var history = Object.values(snapshot.val());
    history
      .sort(function(x, y) {
        return x.timestamp - y.timestamp;
      })
      .reverse();

    return history[0];
  } catch (err) {
    newGroupId = rootGroupId;
    return newGroupId;
  }
}

//getSong("12"); TEST
//getNewSongUrl("iVUcaEHrdVW8iTfNmU4yGUvxbrm1"); TEST


exports.getSongUrl = functions.https.onCall(async (data, context) => {
    let user = context.auth.uid;
    let res = await getNewSongUrl(user);
    console.log(res);

    return res;
});


exports.updateSongScore = functions.https.onCall((data, context) => {
    const songId = data.songId;

    var db = admin.database();
    var ref = db.ref("songs");
    //ref.orderByKey().equalTo(songId).once("value",(song)=>{
    //ref.child(songId).once("value",(song)=>{

    ref.once("value", (songs) => {
        var oldScore;
        var newScore;
        songs.forEach((song) => {
            if (song.key === songId) {
                //console.log(song);
                oldScore = song.val().score;
                //console.log("old score: " + oldScore);
                newScore = parseInt(oldScore);
                newScore++;
                //console.log("new score: " + newScore);
                song.ref.child("score").set(newScore); // update the rellevant song score
                //ref.child(songId).child("score").set(newScore);
            }
        })
    })
});




exports.updateUserSongHistory = functions.https.onCall((data, context) => {
    const uid = context.auth.uid;
    const secondsPlayed = data.sec;
    const isPlayed = data.isPlayed;
    const songId = data.songId;
    const isLiked = data.isLiked;

    var db = admin.database();

    var ref = db.ref("users");
    ref.once("value", (users) => {
        users.forEach((user) => {
            if (user.key === uid) {// we found the rellevant user 
                var songDetails = {
                    songId: songId,
                    secondsPlayed: secondsPlayed,
                    isPlayed: isPlayed,
                    isLiked: isLiked,
                }
                user.ref.child("history").push(songDetails);
            }
        })
    })

});




