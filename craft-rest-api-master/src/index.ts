// Express
import express from 'express';
// Env
import { config } from 'dotenv';
// Mongo
import { connectToDatabaseStats } from './services/playerStatistics.service'
import { connectToDatabaseConnections } from './services/connections.service'
// Cors
import cors from 'cors';
// Controllers
import statisticsRouter from './routes/statistics.route'
import connectionsRouter from './routes/connections.route'

// Initialises env variables
config();

// Variables
const {
    API_PORT,
    DB_CONN_STRING,
    DB_NAME
} = process.env;

// API initialisation
const app = express();

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Mongo
connectToDatabaseStats(DB_CONN_STRING, DB_NAME);
connectToDatabaseConnections(DB_CONN_STRING, DB_NAME)

// Setup routers
app.use('/v1/statistics', statisticsRouter);
app.use('/v1/connections', connectionsRouter);

// Start REST api
app.listen(API_PORT, async () => {
    console.log(`Started Craft Economy REST API on port ${API_PORT}`);
});