//Author: Alex Perry
//Algorithm:
//get: object of songs
//returns: the index of the song with
// random

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
  }

function Algorithm(group){
    let size = Object.keys(group).length;
    let randomIndex = getRandomInt(size - 1);
    let nextSongId = Object.keys(group)[randomIndex];
   
    return nextSongId;
}

module.exports = Algorithm;