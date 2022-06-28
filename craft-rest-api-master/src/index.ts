// Express
import express from 'express';
// Env
import { config } from 'dotenv';
// Mongo
import { connectToMongo, connectToRedis } from './services/database.service';
// Cors
import cors from 'cors';
// Controllers
import statisticsRouter from './routes/statistics.route';
import connectionsRouter from './routes/connections.route';
import transactionsRouter from './routes/transactions.route';
import realestateRouter from './routes/realestate.route';
import nftmarketplaceRouter from './routes/nftmarketplace.route';

// Initialises env variables
config();

// Variables
const { API_PORT, DB_CONN_STRING, DB_NAME, REDIS_CONN_STRING } = process.env;

// API initialisation
const app = express();

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Database
connectToMongo(DB_CONN_STRING, DB_NAME);
connectToRedis(REDIS_CONN_STRING);

// Setup routers
app.use('/v1/statistics', statisticsRouter);
app.use('/v1/connections', connectionsRouter);
app.use('/v1/tx', transactionsRouter);
app.use('/v1/realestate', realestateRouter)
app.use('/v1/nftmarketplace', nftmarketplaceRouter)


var ROUTER_CACHE = {};

// Sends all our API endpoints
app.get('/', (req, res) => {
    if(Object.keys(ROUTER_CACHE).length === 0) {
        // console.log('ROUTER_CACHE was empty, setting to newest routes');
        const urlStart = `${req.protocol}://${req.get('host')}`
        // get all routes from statisticsRouter
        const statisticsRoutes = statisticsRouter.stack.map(({ route }) => `${urlStart}/v1/statistics` + route.path)
        const connectionsRoutes = connectionsRouter.stack.map(({ route }) => `${urlStart}/v1/connections` + route.path)
        const transactionsRoutes = transactionsRouter.stack.map(({ route }) => `${urlStart}/v1/tx` + route.path)
        const realestateRoutes = realestateRouter.stack.map(({ route }) => `${urlStart}/v1/realestate` + route.path)
        const nftmarketplaceRouters = nftmarketplaceRouter.stack.map(({ route }) => `${urlStart}/v1/nftmarketplace` + route.path)
        ROUTER_CACHE = {
            statistics: statisticsRoutes,
            connections: connectionsRoutes,
            transactions: transactionsRoutes,
            realestate: realestateRoutes,
            nftmarketplace: nftmarketplaceRouters,
        }
    }

    // send all routes
    res.json(ROUTER_CACHE)
});

// Start REST api
app.listen(API_PORT, async () => {
    console.log(`Started Craft Economy REST API on port ${API_PORT}`);
});
