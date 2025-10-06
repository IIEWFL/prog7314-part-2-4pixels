// controllers/gamesController.js
const cache = require('../utils/cache');
const { searchGames, getGameById } = require('../helpers/igdbhelper');

// ---------- Trending Games ----------
exports.getTrendingGames = async (req, res) => {
    try {
        const cacheKey = 'trendingGames';
        const cached = cache.get(cacheKey);
        if (cached) return res.json(cached);

        const limit = parseInt(req.query.limit) || 20; // optional query param
        // trending = top rated games
        const games = await searchGames('', limit);

        cache.set(cacheKey, games);
        res.json(games);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ message: 'Failed to fetch trending games' });
    }
};

// ---------- Game by ID ----------
exports.getGameById = async (req, res) => {
    const { id } = req.params;
    const cacheKey = `game_${id}`;

    try {
        const cached = cache.get(cacheKey);
        if (cached) return res.json(cached);

        const game = await getGameById(id);
        cache.set(cacheKey, game);
        res.json(game);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ message: 'Failed to fetch game by ID' });
    }
};

// ---------- Game Screenshots ----------
exports.getGameScreenshots = async (req, res) => {
    const { id } = req.params;
    const cacheKey = `screenshots_${id}`;

    try {
        const cached = cache.get(cacheKey);
        if (cached) return res.json(cached);

        const game = await getGameById(id); // fetch full game
        const screenshots = game.screenshots || [];
        cache.set(cacheKey, screenshots);
        res.json(screenshots);
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ message: 'Failed to fetch game screenshots' });
    }
};