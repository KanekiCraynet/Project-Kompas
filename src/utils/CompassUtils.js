/**
 * Utility functions for compass calculations and accuracy optimization
 */

// Magnetic declination data for Indonesia region (simplified)
const MAGNETIC_DECLINATION_DATA = {
  // Indonesia coordinates and their magnetic declination
  regions: [
    { lat: -11, lng: 95, declination: 0.5 },
    { lat: -6, lng: 105, declination: 0.3 },
    { lat: 6, lng: 95, declination: 0.7 },
    { lat: 6, lng: 141, declination: 1.2 },
  ]
};

/**
 * Calculate magnetic declination based on location
 * @param {Object} location - {latitude, longitude}
 * @returns {number} Magnetic declination in degrees
 */
export const getMagneticDeclination = (location) => {
  if (!location) return 0;
  
  const { latitude, longitude } = location;
  
  // Simple interpolation for Indonesia region
  // In production, use a proper magnetic declination service
  if (latitude >= -11 && latitude <= 6 && longitude >= 95 && longitude <= 141) {
    // Basic approximation for Indonesia
    return 0.5 + (longitude - 95) * 0.01;
  }
  
  return 0;
};

/**
 * Calculate compass heading from magnetometer data
 * @param {Object} magnetometerData - {x, y, z}
 * @param {Object} location - {latitude, longitude}
 * @returns {number} Heading in degrees (0-360)
 */
export const calculateHeading = (magnetometerData, location) => {
  const { x, y, z } = magnetometerData;
  
  // Calculate raw heading
  let heading = Math.atan2(y, x) * (180 / Math.PI);
  
  // Normalize to 0-360 degrees
  heading = heading < 0 ? heading + 360 : heading;
  
  // Apply magnetic declination correction
  const magneticDeclination = getMagneticDeclination(location);
  heading = heading + magneticDeclination;
  
  // Normalize again after correction
  heading = heading % 360;
  heading = heading < 0 ? heading + 360 : heading;
  
  return heading;
};

/**
 * Smooth heading values to reduce jitter
 * @param {number} newHeading - New heading value
 * @param {number} previousHeading - Previous heading value
 * @param {number} smoothingFactor - Smoothing factor (0-1)
 * @returns {number} Smoothed heading
 */
export const smoothHeading = (newHeading, previousHeading, smoothingFactor = 0.1) => {
  if (previousHeading === undefined) return newHeading;
  
  // Handle 360/0 degree boundary
  let diff = newHeading - previousHeading;
  if (diff > 180) diff -= 360;
  if (diff < -180) diff += 360;
  
  return previousHeading + diff * smoothingFactor;
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
 * Validate magnetometer data
 * @param {Object} data - {x, y, z}
 * @returns {boolean} True if data is valid
 */
export const validateMagnetometerData = (data) => {
  if (!data || typeof data.x !== 'number' || typeof data.y !== 'number' || typeof data.z !== 'number') {
    return false;
  }
  
  // Check for reasonable values (magnetometer typically ranges from -100 to 100 microtesla)
  const maxValue = 200;
  return Math.abs(data.x) <= maxValue && 
         Math.abs(data.y) <= maxValue && 
         Math.abs(data.z) <= maxValue;
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
