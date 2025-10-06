const NodeCache = require('node-cache');
const cache = new NodeCache({ stdTTL: 60 * 60 * 24 * process.env.CACHE_DAYS }); // TTL in seconds

module.exports = cache;