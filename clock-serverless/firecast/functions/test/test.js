const functions = require("firebase-functions");

const getSongUrl = require("C:/Users/Aelxander/Desktop/Project/serverless/RiseUp-Alarm/clock-serverless/firecast/functions/functions/getSongUrl");

const updateSongScore = require("C:/Users/Aelxander/Desktop/Project/serverless/RiseUp-Alarm/clock-serverless/firecast/functions/functions/updateSongScore");

const updateUserSongHistory = require("C:/Users/Aelxander/Desktop/Project/serverless/RiseUp-Alarm/clock-serverless/firecast/functions/functions/updateUserSongHistory");

var user = {
  auth: {
    uid: null
  },
  timestamp: 1540358107754
};
var dataHistory = {

};

//regular testing, simulates the workflow of the server with dummy packet
async function runTest() {
  while (true) {
    let song = await getSongUrl(dataHistory, user);
    let data = {
      sec: 36,
      isRanked: "0",
      songId: song.songId,
      isLiked: "0",
      timestamp: Date.now()
    };
    user["timestamp"] = data.timestamp;
    const fs = require("fs");
    var content= fs.readFileSync("firecast/functions/test/answer.txt", 'utf8');

    data["isLiked"] = content;

    await updateUserSongHistory(data, user);

    await updateSongScore(data, user);
  }
}
runTest();
