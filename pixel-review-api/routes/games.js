// routes/games.js
/*freeCodeCamp.org. (2025, June 10). MERN Stack Tutorial for Beginners with Deployment – 2025 [Video]. 
YouTube. https://www.youtube.com/watch?v=F9gB5b4jgOI*/
/*https://api-docs.igdb.com/#getting-started
Endpoints, how to retrieve */

const express = require('express');
const router = express.Router();
const { getTrendingGames, getGameById, getGameScreenshots } = require('../controllers/gamesController');

router.get('/trending', getTrendingGames);
router.get('/:id', getGameById);
router.get('/:id/screenshots', getGameScreenshots);

module.exports = router;