/**
 * Test script for compass accuracy and functionality
 * Run with: node test-compass.js
 */

const { 
  calculateHeading, 
  smoothHeading, 
  getDetailedDirection, 
  validateMagnetometerData,
  getAccuracyStatus 
} = require('./src/utils/CompassUtils');

console.log('🧭 Testing Kompas GPS Functions\n');

// Test 1: Magnetometer data validation
console.log('1. Testing magnetometer data validation:');
const validData = { x: 25.5, y: -12.3, z: 8.7 };
const invalidData = { x: 'invalid', y: 25, z: 30 };
const outOfRangeData = { x: 300, y: -250, z: 100 };

console.log('Valid data:', validateMagnetometerData(validData)); // Should be true
console.log('Invalid data:', validateMagnetometerData(invalidData)); // Should be false
console.log('Out of range data:', validateMagnetometerData(outOfRangeData)); // Should be false

// Test 2: Heading calculation
console.log('\n2. Testing heading calculation:');
const testLocation = { latitude: -6.2, longitude: 106.8 }; // Jakarta
const testMagnetometerData = { x: 20, y: 0, z: 10 }; // Pointing North

const heading = calculateHeading(testMagnetometerData, testLocation);
console.log('Calculated heading:', heading.toFixed(2) + '°');

// Test 3: Direction names
console.log('\n3. Testing direction names:');
const testHeadings = [0, 45, 90, 135, 180, 225, 270, 315];
testHeadings.forEach(heading => {
  const direction = getDetailedDirection(heading);
  console.log(`${heading}°: ${direction.direction} (${direction.cardinal})`);
});

// Test 4: Smoothing function
console.log('\n4. Testing heading smoothing:');
let currentHeading = 0;
for (let i = 0; i < 5; i++) {
  const newHeading = 10 + (i * 5);
  const smoothed = smoothHeading(newHeading, currentHeading, 0.2);
  console.log(`Raw: ${newHeading}°, Smoothed: ${smoothed.toFixed(2)}°`);
  currentHeading = smoothed;
}

// Test 5: Accuracy status
console.log('\n5. Testing accuracy status:');
const testAccuracies = [2, 5, 10, 20];
testAccuracies.forEach(accuracy => {
  const status = getAccuracyStatus(accuracy);
  console.log(`${accuracy}m: ${status.status} (${status.color})`);
});

// Test 6: Magnetic declination simulation
console.log('\n6. Testing magnetic declination for Indonesia:');
const indonesiaLocations = [
  { lat: -6.2, lng: 106.8, name: 'Jakarta' },
  { lat: -7.8, lng: 110.4, name: 'Yogyakarta' },
  { lat: -8.7, lng: 115.2, name: 'Bali' },
  { lat: 3.6, lng: 98.7, name: 'Medan' }
];

indonesiaLocations.forEach(location => {
  const declination = getMagneticDeclination(location);
  console.log(`${location.name}: ${declination.toFixed(2)}°`);
});

function getMagneticDeclination(location) {
  if (!location) return 0;
  
  const { latitude, longitude } = location;
  
  if (latitude >= -11 && latitude <= 6 && longitude >= 95 && longitude <= 141) {
    return 0.5 + (longitude - 95) * 0.01;
  }
  
  return 0;
}

console.log('\n✅ All tests completed!');
console.log('\n📱 To test on device:');
console.log('1. Build APK: ./build.sh');
console.log('2. Install: adb install kompas-gps-debug.apk');
console.log('3. Test in different locations and orientations');
console.log('4. Check accuracy with known landmarks');
