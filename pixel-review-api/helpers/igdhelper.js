/*freeCodeCamp.org. (2025, June 10). MERN Stack Tutorial for Beginners with Deployment – 2025 [Video]. 
YouTube. https://www.youtube.com/watch?v=F9gB5b4jgOI*/

const axios = require('axios');

const TWITCH_CLIENT_ID = process.env.TWITCH_CLIENT_ID;
const TWITCH_CLIENT_SECRET = process.env.TWITCH_CLIENT_SECRET;

let igdbAccessToken = null;
let tokenExpiresAt = 0;

// Get IGDB access token
async function getIgdbToken() {
    const now = Date.now();
    if (igdbAccessToken && now < tokenExpiresAt) return igdbAccessToken;

    try {
        const response = await axios.post('https://id.twitch.tv/oauth2/token', null, {
            params: {
                client_id: TWITCH_CLIENT_ID,
                client_secret: TWITCH_CLIENT_SECRET,
                grant_type: 'client_credentials',
            },
        });
        igdbAccessToken = response.data.access_token;
        tokenExpiresAt = now + response.data.expires_in * 1000;
        return igdbAccessToken;
    } catch (err) {
        console.error('Failed to get IGDB token:', err.response?.data || err.message);
        throw err;
    }
}

// Core IGDB fetch function
async function fetchFromIGDB(body) {
    const token = await getIgdbToken();
    const response = await axios({
        url: 'https://api.igdb.com/v4/games',
        method: 'POST',
        headers: {
            'Client-ID': TWITCH_CLIENT_ID,
            Authorization: `Bearer ${token}`,
        },
        data: body,
    });

    return response.data.map(game => ({
        ...game,
        cover: game.cover ? { url: `https:${game.cover.url.replace('t_thumb', 't_cover_big')}` } : null,
        artworks: game.artworks ? game.artworks.map(a => ({ url: `https:${a.url.replace('t_thumb', 't_screenshot_huge')}` })) : [],
        screenshots: game.screenshots ? game.screenshots.map(s => ({ url: `https:${s.url.replace('t_thumb', 't_screenshot_huge')}` })) : [],
        videos: game.videos ? game.videos.map(v => `https://www.youtube.com/watch?v=${v.video_id}`) : [],
        genres: game.genres ? game.genres.map(g => g.name) : [],
        platforms: game.platforms ? game.platforms.map(p => p.name) : [],
        developers: game.involved_companies ? game.involved_companies.map(c => c.company.name) : [],
    }));
}

// Fetch trending games
async function getTrendingGames(limit = 20) {
    const body = `
        fields id,name,rating,first_release_date,summary,
               cover.url,artworks.url,screenshots.url,
               genres.name,platforms.name,
               involved_companies.company.name,videos.video_id;
        sort rating desc;
        limit ${limit};
    `;
    return await fetchFromIGDB(body);
}

// Search games
async function searchGames(query = '', limit = 20) {
    const body = `
        search "${query}";
        fields id,name,rating,first_release_date,summary,
               cover.url,artworks.url,screenshots.url,
               genres.name,platforms.name,
               involved_companies.company.name,videos.video_id;
        limit ${limit};
    `;
    return await fetchFromIGDB(body);
}

// Get game by ID
async function getGameById(id) {
    const body = `
        fields id,name,rating,first_release_date,summary,
               cover.url,artworks.url,screenshots.url,
               genres.name,platforms.name,
               involved_companies.company.name,videos.video_id;
        where id = ${id};
    `;
    const data = await fetchFromIGDB(body);
    if (!data.length) throw new Error('Game not found');
    return data[0];
}

// Export everything, including fetchFromIGDB
module.exports = { getIgdbToken, fetchFromIGDB, getTrendingGames, searchGames, getGameById };
