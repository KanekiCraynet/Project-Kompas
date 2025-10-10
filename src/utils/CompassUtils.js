/**
 * Advanced utility functions for compass calculations and accuracy optimization
 * Includes complex algorithms for magnetometer calibration, filtering, and accuracy assessment
 */

// Enhanced magnetic declination data for Indonesia region
const MAGNETIC_DECLINATION_DATA = {
  regions: [
    { lat: -11, lng: 95, declination: 0.5, accuracy: 0.1 },
    { lat: -6, lng: 105, declination: 0.3, accuracy: 0.1 },
    { lat: 6, lng: 95, declination: 0.7, accuracy: 0.1 },
    { lat: 6, lng: 141, declination: 1.2, accuracy: 0.1 },
    { lat: -8.5, lng: 115, declination: 0.4, accuracy: 0.1 }, // Bali
    { lat: -6.2, lng: 106.8, declination: 0.2, accuracy: 0.1 }, // Jakarta
    { lat: -7.8, lng: 110.4, declination: 0.3, accuracy: 0.1 }, // Yogyakarta
  ]
};

// Calibration parameters
const CALIBRATION_THRESHOLD = 0.1;
const MIN_CALIBRATION_SAMPLES = 50;
const MAX_CALIBRATION_SAMPLES = 200;

// Filtering parameters
const LOW_PASS_ALPHA = 0.8;
const HIGH_PASS_ALPHA = 0.1;
const KALMAN_Q = 0.1;
const KALMAN_R = 0.1;

/**
 * Calculate magnetic declination based on location with improved accuracy
 * @param {Object} location - {latitude, longitude}
 * @returns {Object} {declination, accuracy, source}
 */
export const getMagneticDeclination = (location) => {
  if (!location) {
    return { declination: 0, accuracy: 1.0, source: 'default' };
  }
  
  const { latitude, longitude } = location;
  
  // Find closest region for interpolation
  let closestRegion = null;
  let minDistance = Infinity;
  
  for (const region of MAGNETIC_DECLINATION_DATA.regions) {
    const distance = Math.sqrt(
      Math.pow(latitude - region.lat, 2) + Math.pow(longitude - region.lng, 2)
    );
    if (distance < minDistance) {
      minDistance = distance;
      closestRegion = region;
    }
  }
  
  if (closestRegion && minDistance < 5) {
    // Use closest region if within 5 degrees
    return {
      declination: closestRegion.declination,
      accuracy: closestRegion.accuracy,
      source: 'regional_data'
    };
  }
  
  // Fallback to simple approximation for Indonesia
  if (latitude >= -11 && latitude <= 6 && longitude >= 95 && longitude <= 141) {
    const declination = 0.5 + (longitude - 95) * 0.01;
    return {
      declination,
      accuracy: 0.5,
      source: 'approximation'
    };
  }
  
  return { declination: 0, accuracy: 1.0, source: 'default' };
};

/**
 * Calculate compass heading from magnetometer data with advanced algorithms and error handling
 * @param {Object} magnetometerData - {x, y, z}
 * @param {Object} location - {latitude, longitude}
 * @param {boolean} isCalibrated - Whether magnetometer is calibrated
 * @returns {number} Heading in degrees (0-360)
 */
export const calculateHeading = (magnetometerData, location, isCalibrated = false) => {
  try {
    if (!magnetometerData || typeof magnetometerData.x !== 'number' || 
        typeof magnetometerData.y !== 'number' || typeof magnetometerData.z !== 'number') {
      console.warn('Invalid magnetometer data for heading calculation');
      return 0;
    }
    
    const { x, y, z } = magnetometerData;
    
    // Calculate magnetic field strength safely
    let fieldStrength;
    try {
      fieldStrength = Math.sqrt(x * x + y * y + z * z);
    } catch (calcError) {
      console.error('Error calculating field strength:', calcError);
      return 0;
    }
    
    // Check for reasonable field strength (typical range: 20-60 microtesla)
    if (!isFinite(fieldStrength) || fieldStrength < 5 || fieldStrength > 150) {
      console.warn('Unusual magnetic field strength:', fieldStrength);
      return 0;
    }
    
    // Calculate raw heading using atan2 with error handling
    let heading;
    try {
      heading = Math.atan2(y, x) * (180 / Math.PI);
    } catch (atanError) {
      console.error('Error calculating atan2:', atanError);
      return 0;
    }
    
    // Normalize to 0-360 degrees
    heading = heading < 0 ? heading + 360 : heading;
    
    // Apply magnetic declination correction safely
    try {
      const declinationData = getMagneticDeclination(location);
      heading = heading + declinationData.declination;
    } catch (declinationError) {
      console.warn('Error applying magnetic declination:', declinationError);
      // Continue without declination correction
    }
    
    // Apply tilt compensation if accelerometer data is available
    try {
      const tiltCompensation = calculateTiltCompensation(magnetometerData);
      heading = heading + tiltCompensation;
    } catch (tiltError) {
      console.warn('Error applying tilt compensation:', tiltError);
      // Continue without tilt compensation
    }
    
    // Normalize again after corrections
    heading = heading % 360;
    heading = heading < 0 ? heading + 360 : heading;
    
    // Final validation
    if (!isFinite(heading) || heading < 0 || heading > 360) {
      console.warn('Invalid heading calculated:', heading);
      return 0;
    }
    
    return Math.round(heading * 100) / 100; // Round to 2 decimal places
  } catch (error) {
    console.error('Error in calculateHeading:', error);
    return 0;
  }
};

/**
 * Calculate tilt compensation (simplified version)
 * @param {Object} magnetometerData - {x, y, z}
 * @returns {number} Tilt compensation in degrees
 */
const calculateTiltCompensation = (magnetometerData) => {
  const { x, y, z } = magnetometerData;
  const fieldStrength = Math.sqrt(x * x + y * y + z * z);
  
  // Simple tilt estimation based on Z component
  // In production, use accelerometer data for accurate tilt compensation
  const tiltAngle = Math.asin(z / fieldStrength) * (180 / Math.PI);
  
  // Return small compensation factor
  return tiltAngle * 0.1;
};

/**
 * Advanced smoothing algorithm for heading values
 * @param {number} newHeading - New heading value
 * @param {number} previousHeading - Previous heading value
 * @param {number} smoothingFactor - Smoothing factor (0-1)
 * @param {number} velocity - Rate of change
 * @returns {number} Smoothed heading
 */
export const smoothHeading = (newHeading, previousHeading, smoothingFactor = 0.1, velocity = 0) => {
  if (previousHeading === undefined) return newHeading;
  
  // Handle 360/0 degree boundary
  let diff = newHeading - previousHeading;
  if (diff > 180) diff -= 360;
  if (diff < -180) diff += 360;
  
  // Adaptive smoothing based on velocity
  const adaptiveFactor = Math.min(smoothingFactor + Math.abs(velocity) * 0.1, 0.9);
  
  // Apply exponential smoothing
  const smoothed = previousHeading + diff * adaptiveFactor;
  
  // Normalize result
  return smoothed < 0 ? smoothed + 360 : smoothed % 360;
};

/**
 * Kalman filter for magnetometer data
 * @param {Object} measurement - {x, y, z}
 * @param {Object} previousState - Previous filter state
 * @returns {Object} Filtered data and new state
 */
export const kalmanFilter = (measurement, previousState = null) => {
  if (!previousState) {
    return {
      filtered: measurement,
      state: {
        x: measurement.x,
        y: measurement.y,
        z: measurement.z,
        P: 1.0
      }
    };
  }
  
  const { x, y, z, P } = previousState;
  
  // Prediction step
  const P_pred = P + KALMAN_Q;
  
  // Update step
  const K = P_pred / (P_pred + KALMAN_R);
  
  const filtered = {
    x: x + K * (measurement.x - x),
    y: y + K * (measurement.y - y),
    z: z + K * (measurement.z - z)
  };
  
  const newState = {
    x: filtered.x,
    y: filtered.y,
    z: filtered.z,
    P: (1 - K) * P_pred
  };
  
  return { filtered, state: newState };
};

/**
 * Get direction name from heading
 * @param {number} heading - Heading in degrees
 * @returns {string} Direction name
 */
export const getDirectionName = (heading) => {
  const directions = [
    'Utara', 'Utara-Timur', 'Timur', 'Tenggara',
    'Selatan', 'Barat Daya', 'Barat', 'Barat Laut'
  ];
  
  const index = Math.round(heading / 45) % 8;
  return directions[index];
};

/**
 * Get detailed direction with degrees
 * @param {number} heading - Heading in degrees
 * @returns {Object} {direction, degrees, cardinal}
 */
export const getDetailedDirection = (heading) => {
  const direction = getDirectionName(heading);
  const degrees = Math.round(heading);
  
  let cardinal = 'N';
  if (heading >= 337.5 || heading < 22.5) cardinal = 'N';
  else if (heading >= 22.5 && heading < 67.5) cardinal = 'NE';
  else if (heading >= 67.5 && heading < 112.5) cardinal = 'E';
  else if (heading >= 112.5 && heading < 157.5) cardinal = 'SE';
  else if (heading >= 157.5 && heading < 202.5) cardinal = 'S';
  else if (heading >= 202.5 && heading < 247.5) cardinal = 'SW';
  else if (heading >= 247.5 && heading < 292.5) cardinal = 'W';
  else if (heading >= 292.5 && heading < 337.5) cardinal = 'NW';
  
  return { direction, degrees, cardinal };
};

/**
 * Calculate distance between two coordinates
 * @param {Object} coord1 - {latitude, longitude}
 * @param {Object} coord2 - {latitude, longitude}
 * @returns {number} Distance in meters
 */
export const calculateDistance = (coord1, coord2) => {
  const R = 6371e3; // Earth's radius in meters
  const φ1 = coord1.latitude * Math.PI / 180;
  const φ2 = coord2.latitude * Math.PI / 180;
  const Δφ = (coord2.latitude - coord1.latitude) * Math.PI / 180;
  const Δλ = (coord2.longitude - coord1.longitude) * Math.PI / 180;

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ/2) * Math.sin(Δλ/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

  return R * c;
};

/**
 * Advanced validation for magnetometer data with enhanced error checking
 * @param {Object} data - {x, y, z}
 * @returns {Object} {isValid, reason, quality}
 */
export const validateMagnetometerData = (data) => {
  try {
    // Check if data exists and has required properties
    if (!data || typeof data !== 'object') {
      return { isValid: false, reason: 'No data provided', quality: 0 };
    }
    
    // Check for required properties
    if (typeof data.x !== 'number' || typeof data.y !== 'number' || typeof data.z !== 'number') {
      return { isValid: false, reason: 'Invalid data types', quality: 0 };
    }
    
    // Check for NaN or Infinity
    if (!isFinite(data.x) || !isFinite(data.y) || !isFinite(data.z)) {
      return { isValid: false, reason: 'Non-finite values', quality: 0 };
    }
    
    // Check for reasonable values (magnetometer typically ranges from -100 to 100 microtesla)
    const maxValue = 200;
    const minValue = 5;
    
    if (Math.abs(data.x) > maxValue || Math.abs(data.y) > maxValue || Math.abs(data.z) > maxValue) {
      return { isValid: false, reason: 'Values too large', quality: 0 };
    }
    
    // Calculate field strength safely
    let fieldStrength;
    try {
      fieldStrength = Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
    } catch (calcError) {
      return { isValid: false, reason: 'Calculation error', quality: 0 };
    }
    
    if (!isFinite(fieldStrength) || fieldStrength < minValue) {
      return { isValid: false, reason: 'Field strength too low or invalid', quality: 0 };
    }
    
    // Calculate quality score with enhanced logic
    let quality = 1.0;
    
    // Penalize if field strength is unusual (typical range: 20-60 microtesla)
    if (fieldStrength < 15 || fieldStrength > 80) {
      quality *= 0.3;
    } else if (fieldStrength < 20 || fieldStrength > 60) {
      quality *= 0.6;
    }
    
    // Penalize if values are too close to zero (indicates sensor issues)
    if (Math.abs(data.x) < 0.5 || Math.abs(data.y) < 0.5) {
      quality *= 0.4;
    }
    
    // Check for sensor saturation
    if (Math.abs(data.x) > 150 || Math.abs(data.y) > 150 || Math.abs(data.z) > 150) {
      quality *= 0.5;
    }
    
    return { 
      isValid: true, 
      reason: 'Valid data', 
      quality: Math.max(0, Math.min(1, quality)),
      fieldStrength: Math.round(fieldStrength * 100) / 100
    };
  } catch (error) {
    console.error('Error in validateMagnetometerData:', error);
    return { isValid: false, reason: 'Validation error', quality: 0 };
  }
};

/**
 * Get accuracy status based on GPS accuracy
 * @param {number} accuracy - GPS accuracy in meters
 * @returns {Object} {status, color, description}
 */
export const getAccuracyStatus = (accuracy) => {
  if (accuracy <= 3) {
    return {
      status: 'Sangat Akurat',
      color: '#4CAF50',
      description: 'GPS akurat dalam 3 meter'
    };
  } else if (accuracy <= 8) {
    return {
      status: 'Akurat',
      color: '#8BC34A',
      description: 'GPS akurat dalam 8 meter'
    };
  } else if (accuracy <= 15) {
    return {
      status: 'Cukup Akurat',
      color: '#FFC107',
      description: 'GPS akurat dalam 15 meter'
    };
  } else {
    return {
      status: 'Kurang Akurat',
      color: '#FF5722',
      description: 'GPS akurat lebih dari 15 meter'
    };
  }
};

/**
 * Calibrate magnetometer using collected data
 * @param {Array} calibrationData - Array of magnetometer readings
 * @returns {Object} Calibration result
 */
export const calibrateMagnetometer = (calibrationData) => {
  if (calibrationData.length < MIN_CALIBRATION_SAMPLES) {
    return {
      isCalibrated: false,
      reason: 'Insufficient data',
      samples: calibrationData.length
    };
  }
  
  // Calculate statistics
  const xValues = calibrationData.map(d => d.x);
  const yValues = calibrationData.map(d => d.y);
  const zValues = calibrationData.map(d => d.z);
  
  const xMean = xValues.reduce((a, b) => a + b, 0) / xValues.length;
  const yMean = yValues.reduce((a, b) => a + b, 0) / yValues.length;
  const zMean = zValues.reduce((a, b) => a + b, 0) / zValues.length;
  
  const xStd = Math.sqrt(xValues.reduce((sum, x) => sum + Math.pow(x - xMean, 2), 0) / xValues.length);
  const yStd = Math.sqrt(yValues.reduce((sum, y) => sum + Math.pow(y - yMean, 2), 0) / yValues.length);
  const zStd = Math.sqrt(zValues.reduce((sum, z) => sum + Math.pow(z - zMean, 2), 0) / zValues.length);
  
  // Check if calibration is good enough
  const isCalibrated = xStd < CALIBRATION_THRESHOLD && yStd < CALIBRATION_THRESHOLD && zStd < CALIBRATION_THRESHOLD;
  
  return {
    isCalibrated,
    reason: isCalibrated ? 'Calibration successful' : 'High variance detected',
    samples: calibrationData.length,
    statistics: {
      xMean, yMean, zMean,
      xStd, yStd, zStd
    },
    offsets: {
      x: -xMean,
      y: -yMean,
      z: -zMean
    }
  };
};

/**
 * Apply low-pass filter to magnetometer data with error handling
 * @param {Object} newData - New magnetometer reading
 * @param {Object} previousData - Previous filtered data
 * @param {number} alpha - Filter coefficient
 * @returns {Object} Filtered data
 */
export const filterMagnetometerData = (newData, previousData, alpha = LOW_PASS_ALPHA) => {
  try {
    if (!newData || typeof newData.x !== 'number' || 
        typeof newData.y !== 'number' || typeof newData.z !== 'number') {
      console.warn('Invalid newData for filtering');
      return previousData || { x: 0, y: 0, z: 0 };
    }
    
    if (!previousData) {
      return {
        x: newData.x,
        y: newData.y,
        z: newData.z
      };
    }
    
    // Validate alpha value
    if (!isFinite(alpha) || alpha < 0 || alpha > 1) {
      alpha = LOW_PASS_ALPHA;
    }
    
    const filtered = {
      x: alpha * newData.x + (1 - alpha) * previousData.x,
      y: alpha * newData.y + (1 - alpha) * previousData.y,
      z: alpha * newData.z + (1 - alpha) * previousData.z
    };
    
    // Validate filtered result
    if (!isFinite(filtered.x) || !isFinite(filtered.y) || !isFinite(filtered.z)) {
      console.warn('Filtered data contains non-finite values, returning newData');
      return newData;
    }
    
    return filtered;
  } catch (error) {
    console.error('Error in filterMagnetometerData:', error);
    return newData || { x: 0, y: 0, z: 0 };
  }
};

/**
 * Calculate compass accuracy based on heading history with enhanced error handling
 * @param {Array} headingHistory - Array of {heading, timestamp}
 * @returns {number} Accuracy score (0-1)
 */
export const calculateCompassAccuracy = (headingHistory) => {
  try {
    if (!Array.isArray(headingHistory) || headingHistory.length < 5) {
      return 0;
    }
    
    // Calculate variance in recent readings (use fewer readings for better performance)
    const recentReadings = headingHistory.slice(-15);
    const headings = recentReadings.map(r => {
      if (typeof r.heading !== 'number' || !isFinite(r.heading)) {
        return 0;
      }
      return r.heading;
    }).filter(h => h >= 0 && h <= 360);
    
    if (headings.length < 3) {
      return 0;
    }
    
    // Handle 360/0 boundary safely
    const normalizedHeadings = headings.map((h, index) => {
      if (index === 0) return h;
      const diff = h - headings[0];
      return diff > 180 ? h - 360 : diff < -180 ? h + 360 : h;
    });
    
    // Calculate mean safely
    const sum = normalizedHeadings.reduce((a, b) => {
      if (!isFinite(a) || !isFinite(b)) return 0;
      return a + b;
    }, 0);
    
    if (!isFinite(sum)) {
      return 0;
    }
    
    const mean = sum / normalizedHeadings.length;
    
    // Calculate variance safely
    const variance = normalizedHeadings.reduce((sum, h) => {
      if (!isFinite(h) || !isFinite(mean)) return sum;
      return sum + Math.pow(h - mean, 2);
    }, 0) / normalizedHeadings.length;
    
    if (!isFinite(variance) || variance < 0) {
      return 0;
    }
    
    const stdDev = Math.sqrt(variance);
    
    if (!isFinite(stdDev)) {
      return 0;
    }
    
    // Convert to accuracy score (lower std dev = higher accuracy)
    // Use 25 degrees as threshold for better sensitivity
    const accuracy = Math.max(0, Math.min(1, 1 - (stdDev / 25)));
    
    return Math.round(accuracy * 100) / 100; // Round to 2 decimal places
  } catch (error) {
    console.error('Error in calculateCompassAccuracy:', error);
    return 0;
  }
};
