// igdb.js
const express = require('express');
const router = express.Router();
const { getTrendingGames, searchGames, getGameById, fetchFromIGDB } = require('../helpers/igdhelper');

// IGDB-supported genres - IGDB genre IDs mapping
const genreMap = {
    "Pinball": [28],
    "Adventure": [31],
    "Indie": [34],
    "Arcade": [30],
    "Visual Novel": [35],
    "Card & Board Game": [33],
    "MOBA": [37],
    "Point-and-click": [38],
    "Fighting": [6],
    "Shooter": [5],
    "Music": [36],
    "Platform": [8],
    "Puzzle": [9],
    "Racing": [10],
    "Real Time Strategy (RTS)": [15],
    "Role-playing (RPG)": [12],
    "Simulator": [16],
    "Sport": [14],
    "Strategy": [15],
    "Turn-based strategy (TBS)": [27],
    "Tactical": [24],
    "Hack and slash/Beat 'em up": [18],
    "Quiz/Trivia": [39]
};

// Trending games
router.get('/trending', async (req, res) => {
    try {
        const games = await getTrendingGames();
        res.json(games);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch trending games' });
    }
});

// Search games
router.get('/search', async (req, res) => {
    try {
        const query = req.query.q || '';
        const limit = parseInt(req.query.limit) || 20;
        const games = await searchGames(query, limit);
        res.json(games);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to search games' });
    }
});

// Upcoming games
router.get('/upcoming', async (req, res) => {
    try {
        const limit = parseInt(req.query.limit) || 10;
        const body = `
            fields id, name, first_release_date, cover.url, genres.name, platforms.name, involved_companies.company.name, videos.video_id;
            where first_release_date > ${Math.floor(Date.now() / 1000)};
            sort first_release_date asc;
            limit ${limit};
        `;
        const games = await fetchFromIGDB(body);
        res.json(games);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch upcoming games' });
    }
});

// New releases
router.get('/new-releases', async (req, res) => {
    try {
        const limit = parseInt(req.query.limit) || 10;
        const body = `
            fields id, name, first_release_date, cover.url, genres.name, platforms.name, involved_companies.company.name, videos.video_id;
            where first_release_date <= ${Math.floor(Date.now() / 1000)};
            sort first_release_date desc;
            limit ${limit};
        `;
        const games = await fetchFromIGDB(body);
        res.json(games);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch new releases' });
    }
});

// All games with optional genre filter (supports multiple genres)
router.get('/all-games', async (req, res) => {
    try {
        const limit = parseInt(req.query.limit) || 50;
        const offset = parseInt(req.query.offset) || 0;
        const genreQuery = req.query.genre; // comma-separated list

        let genreIds = [];
        if (genreQuery) {
            const genres = genreQuery.split(',').map(g => g.trim());
            genres.forEach(g => {
                const ids = genreMap[g];
                if (ids) genreIds.push(...ids);
            });
            if (genreIds.length === 0) return res.status(400).json({ message: 'Unknown genre(s)' });
        }

        let body = `
            fields id, name, first_release_date, cover.url, genres.name, platforms.name, involved_companies.company.name, rating;
        `;

        if (genreIds.length > 0) {
            // Match any of the selected genres
            body += `where genres = (${[...new Set(genreIds)].join(',')});\n`;
        }

        body += `
            sort first_release_date desc;
            limit ${limit};
            offset ${offset};
        `;

        const games = await fetchFromIGDB(body);
        res.json(games);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch all games' });
    }
});

// Popular games based on optional genres
router.get('/popular', async (req, res) => {
    try {
        const limit = parseInt(req.query.limit) || 10;
        const offset = parseInt(req.query.offset) || 0;
        const genreQuery = req.query.genre; // optional comma-separated list

        let genreIds = [];
        if (genreQuery) {
            const genres = genreQuery.split(',').map(g => g.trim());
            genres.forEach(g => {
                const ids = genreMap[g];
                if (ids) genreIds.push(...ids);
            });
            if (genreIds.length === 0)
                return res.status(400).json({ message: 'Unknown genre(s)' });
        }

        // Build IGDB query
        let body = `
fields id, name, first_release_date, cover.url, genres.name, platforms.name, involved_companies.company.name, rating, total_rating, total_rating_count;
`;

        if (genreIds.length > 0) {
            body += `where genres = (${[...new Set(genreIds)].join(',')});\n`;
        }

        body += `
sort total_rating desc;
limit ${limit};
offset ${offset};
`;

        const games = await fetchFromIGDB(body);
        res.json(games);

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch popular games' });
    }
});

// Game details by ID
router.get('/:id', async (req, res) => {
    try {
        const id = parseInt(req.params.id);
        const game = await getGameById(id);
        res.json(game);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch game by ID' });
    }
});

module.exports = router;
