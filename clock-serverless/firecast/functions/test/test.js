const functions = require("firebase-functions");
const path = require('path')

const currPath = path.resolve(path.join(__dirname, '..', 'functions'));

const getSongUrl = require(path.join(currPath,"getSongUrl"));

const updateSongScore = require(path.join(currPath,"updateSongScore"));

const updateUserSongHistory = require(path.join(currPath,"updateUserSongHistory"));

var user = {
  auth: {
    uid: "XUna4YABSHT92mns6Pi5wTHeeDD3"
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
    var content= fs.readFileSync("firecast/functions/functions/test/answer.txt", 'utf8');

    data["isLiked"] = content;

    await updateUserSongHistory(data, user);

    await updateSongScore(data, user);
  }
}
runTest();
