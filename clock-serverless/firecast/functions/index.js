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
    let found = false;
    let lastSongIndex = 0;
    let size;
    let nextGroup;
    while (!found) {
      let lastLikedSong = await getLastLikedSong(user, lastSongIndex++);
      let currGroup = lastLikedSong == null ? rootGroupId : lastLikedSong.songId;
      nextGroup = await getNextGroup(currGroup);
      let filteredNextGroup = await filterGroup(nextGroup, user);
      size = Object.keys(filteredNextGroup).length;
      found = size > 0 ? true : false;
    }
  
    let randomIndex = getRandomInt(size - 1);
    let nextSongId = Object.keys(nextGroup)[randomIndex];
    let nextSongUrl = nextGroup[nextSongId].url; //TODO: remove unliked songs
    return {
      url: nextSongUrl,
      songId: nextSongId
    };
  }

async function filterGroup(group, user) {
  let songHistoryQuery = db
    .ref("/users")
    .child(user)
    .child("history");
  let snapshot = await songHistoryQuery.once("value");
  let historyGroup = snapshot.val();

  for (let song in historyGroup) {
    if (group[historyGroup[song].songId] != undefined) {
      delete group[historyGroup[song].songId];
    }
  }

  return group;
}

async function getNextGroup(lastSongId) {
  //first tries to get the group that the current song brought to the db
  //(song's children in tree). we assume it's not a leaf.
  let nextGroupID = Number.parseInt(lastSongId);
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

async function getLastLikedSong(user, index) {
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
    if (index == history.length) {
      throw new Error("No more history");
    }
    return history[index];
  } catch (err) {
    newGroupId = null;
    return newGroupId;
  }
}
exports.getSongUrl = functions.https.onCall(async (data, context) => {
  let user = context.auth.uid;
  let res = await getNewSongUrl(user);

  return res;
});

exports.updateSongScore = functions.https.onCall(async (data, context) => {
  try {
    let updates = {};
    const songId = data.songId;
    let uid = context.auth.uid;
    let isLiked = data.isLiked;
    //update the song's data
    let currSongQuery = db.ref("songs/" + songId);
    let currSong = await getSong(songId);
    let newScore = isLiked == 1 ? currSong.score + 1 : currSong.score;
    let incCount = ++currSong.count;
    currSong.score = newScore;
    currSong.count = incCount;
    updates["songs/" + songId] = currSong;
    //update user's history
    const getSongHistoryIdQuery = db
      .ref("users/" + uid)
      .child("history")
      .orderByChild("songId")
      .equalTo(songId);
    let snapshot = await getSongHistoryIdQuery.once("value");
    let historySongLog = Object.values(snapshot.val())[0];
    let historySongId = Object.keys(snapshot.val())[0];
    historySongLog.isLiked = isLiked;
    historySongLog.isRanked = "1";
    updates["users/" + uid + "/history/" + historySongId] = historySongLog;

    await db.ref().update(updates);
  } catch (err) {
    throw new functions.https.HttpsError("Failed to update song score", err);
  }
});

exports.updateUserSongHistory = functions.https.onCall(
  async (data, context) => {
    let uid = context.auth.uid;
    const logDetails = {
      secondsPlayed: data.sec,
      isRanked: "0",
      songId: data.songId,
      isLiked: "0",
      timestamp: Date.now()
    };

    const query = db.ref("users/" + uid).child("history");
    try {
      console.log(logDetails);
      await query.push(logDetails);
    } catch (err) {
      console.error(err);
      throw new functions.https.HttpsError("", err);
    }
  }
);
