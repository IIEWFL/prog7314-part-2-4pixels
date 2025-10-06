/*freeCodeCamp.org. (2025, June 10). MERN Stack Tutorial for Beginners with Deployment – 2025 [Video]. 
YouTube. https://www.youtube.com/watch?v=F9gB5b4jgOI*/

/*GeekProbin. (2023, August 19). React JS Gaming Website Tutorial With RAWG Video Games API | PART 1 | React For Beginners [Video].
 YouTube. https://www.youtube.com/watch?v=TuOF8ppiKDY */

/*https://api-docs.igdb.com/#getting-started
Endpoints, how to retrieve */

const express = require('express');
const dotenv = require('dotenv');
const cors = require('cors');
const axios = require('axios');

dotenv.config();

const gamesRoutes = require('./routes/games'); // existing routes
const igdbRoutes = require('./routes/igdb');   // IGDB routes

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/games', gamesRoutes);
app.use('/api/igdb', igdbRoutes); // mount IGDB routes

// ---------- IGDB Setup (for testing only, could move to controller) ----------
const TWITCH_CLIENT_ID = process.env.TWITCH_CLIENT_ID;
const TWITCH_CLIENT_SECRET = process.env.TWITCH_CLIENT_SECRET;

let igdbAccessToken = null;
let tokenExpiresAt = 0;

// Helper: fetch IGDB access token
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
        tokenExpiresAt = now + response.data.expires_in * 1000; // token expiry in ms
        return igdbAccessToken;
    } catch (err) {
        console.error('Failed to get IGDB token:', err.message);
        throw err;
    }
}

// Start server
const PORT = process.env.PORT || 5000;
app.listen(PORT, '0.0.0.0', () => console.log(`Server running on port ${PORT}`));