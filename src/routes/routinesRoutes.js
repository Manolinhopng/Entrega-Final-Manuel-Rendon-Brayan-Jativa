const express = require('express');
const router = express.Router();
const { getRoutines, createRoutine } = require('../controllers/routinesController');

router.get('/', getRoutines);
router.post('/', createRoutine);

module.exports = router;