//cache.cs
/*freeCodeCamp.org. (2025, June 10). MERN Stack Tutorial for Beginners with Deployment – 2025 [Video]. 
YouTube. https://www.youtube.com/watch?v=F9gB5b4jgOI*/
const NodeCache = require('node-cache');
const cache = new NodeCache({ stdTTL: 60 * 60 * 24 * process.env.CACHE_DAYS }); // TTL in seconds

module.exports = cache;