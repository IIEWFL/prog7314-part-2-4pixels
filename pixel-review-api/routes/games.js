// routes/games.js
const express = require('express');
const router = express.Router();
const { getTrendingGames, getGameById, getGameScreenshots } = require('../controllers/gamesController');

router.get('/trending', getTrendingGames);
router.get('/:id', getGameById);
router.get('/:id/screenshots', getGameScreenshots);

module.exports = router;