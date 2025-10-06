//igdbService.cs

/*freeCodeCamp.org. (2025, June 10). MERN Stack Tutorial for Beginners with Deployment – 2025 [Video]. 
YouTube. https://www.youtube.com/watch?v=F9gB5b4jgOI*/
const axios = require('axios');

const CLIENT_ID = process.env.TWITCH_CLIENT_ID;
const CLIENT_SECRET = process.env.TWITCH_CLIENT_SECRET;
let accessToken = null;

// Get a Twitch access token
async function getAccessToken() {
    const response = await axios.post('https://id.twitch.tv/oauth2/token', null, {
        params: {
            client_id: CLIENT_ID,
            client_secret: CLIENT_SECRET,
            grant_type: 'client_credentials'
        }
    });
    accessToken = response.data.access_token;
    return accessToken;
}

// Search games
async function searchGames(query, limit = 20) {
    if (!accessToken) await getAccessToken();
    const body = `search "${query}"; fields name,cover.url,first_release_date,platforms.name; limit ${limit};`;

    const response = await axios({
        url: 'https://api.igdb.com/v4/games',
        method: 'POST',
        headers: {
            'Client-ID': CLIENT_ID,
            'Authorization': `Bearer ${accessToken}`,
        },
        data: body
    });

    return response.data;
}

module.exports = { searchGames };