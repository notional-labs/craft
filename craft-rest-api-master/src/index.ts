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
import nftsRouter from './routes/nfts.route';
import daoRouter from './routes/dao.route';

// Initializes env variables
config();

// Variables
const { API_PORT, DB_CONN_STRING, DB_NAME, REDIS_CONN_STRING, DAO_EXP_MODULE_ONLY } = process.env;

// API initialization
const app = express();

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Cache
connectToRedis(REDIS_CONN_STRING);

//  routers
var ROUTER_CACHE = {};

// Setup DAO/EXP module only for price oracle
if(DAO_EXP_MODULE_ONLY && DAO_EXP_MODULE_ONLY.toLowerCase().startsWith('t')) {
    console.log(`DAO_EXP_MODULE_ONLY is ${DAO_EXP_MODULE_ONLY}`);
    app.use('/v1/dao', daoRouter)
} else {
    // we only need MOngoDB if we use all the routes, excluding DAO/EXP
    connectToMongo(DB_CONN_STRING, DB_NAME);
    // Setup all routers (including the DAO)
    app.use('/v1/statistics', statisticsRouter);
    app.use('/v1/connections', connectionsRouter);
    app.use('/v1/tx', transactionsRouter);
    app.use('/v1/marketplace', nftmarketplaceRouter)
    app.use('/v1/realestate', realestateRouter)
    app.use('/v1/nfts', nftsRouter)
    app.use('/v1/dao', daoRouter)
}



// Sends all our API endpoints
app.get('/', (req, res) => {
    if(Object.keys(ROUTER_CACHE).length === 0) {

        // there has to be a better way to do this..
        const urlStart = `${req.protocol}://${req.get('host')}`
        if(DAO_EXP_MODULE_ONLY && DAO_EXP_MODULE_ONLY.toLowerCase().startsWith('t')) {
            const daoRoutes = daoRouter.stack.map(({ route }) => `${urlStart}/v1/dao` + route.path)
            ROUTER_CACHE = { dao: daoRoutes }
        } else {
            // Setup all endpoints, including the DAO endpoint
            const statisticsRoutes = statisticsRouter.stack.map(({ route }) => `${urlStart}/v1/statistics` + route.path)
            const connectionsRoutes = connectionsRouter.stack.map(({ route }) => `${urlStart}/v1/connections` + route.path)
            const transactionsRoutes = transactionsRouter.stack.map(({ route }) => `${urlStart}/v1/tx` + route.path)
            const nftmarketplaceRouters = nftmarketplaceRouter.stack.map(({ route }) => `${urlStart}/v1/marketplace` + route.path)
            const nftsRoutes = nftsRouter.stack.map(({ route }) => `${urlStart}/v1/nfts` + route.path)
            const realestateRoutes = realestateRouter.stack.map(({ route }) => `${urlStart}/v1/realestate` + route.path)
            const daoRoutes = daoRouter.stack.map(({ route }) => `${urlStart}/v1/dao` + route.path)            
            ROUTER_CACHE = {
                statistics: statisticsRoutes,
                connections: connectionsRoutes,
                transactions: transactionsRoutes,            
                nftmarketplace: nftmarketplaceRouters,
                nfts: nftsRoutes,
                realestate: realestateRoutes,
                dao: daoRoutes
            }
        }
    }

    // send all routes
    res.json(ROUTER_CACHE)
});

// Start REST api
app.listen(API_PORT, async () => {
    console.log(`Started Craft Economy REST API on port ${API_PORT}`);
});
