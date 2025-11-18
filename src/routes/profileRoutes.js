// src/routes/profileRoutes.js
const express = require('express');
const router = express.Router();
const { getProfile, updateProfile, updateSpecificFields } = require('../controllers/profileController');

router.get('/:userId', getProfile);
router.put('/:userId', updateProfile);
router.post('/:userId/update-field', updateSpecificFields); // opcional

module.exports = router;