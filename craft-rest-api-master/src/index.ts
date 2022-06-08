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

// Sends all our API endpoints
app.get('/', (req, res) => {
    res.send(`/v1/statistics , /v1/connections , /v1/tx, /v1/realestate`);
});

// Start REST api
app.listen(API_PORT, async () => {
    console.log(`Started Craft Economy REST API on port ${API_PORT}`);
});
