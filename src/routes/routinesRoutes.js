// src/routes/routinesRoutes.js
const express = require('express');
const router = express.Router();
const { 
  getRoutines, 
  createRoutine, 
  updateRoutine, 
  deleteRoutine 
} = require('../controllers/routinesController');

router.get('/', getRoutines);
router.post('/', createRoutine);
router.put('/:routineId', updateRoutine);
router.delete('/:routineId', deleteRoutine);

module.exports = router;