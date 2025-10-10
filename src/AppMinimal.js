import React, {useState, useEffect} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  StatusBar,
  View,
  Text,
  Dimensions,
} from 'react-native';

const {width, height} = Dimensions.get('window');

const AppMinimal = () => {
  const [heading, setHeading] = useState(0);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    // Simulate compass initialization
    const timer = setTimeout(() => {
      setIsInitialized(true);
    }, 1000);

    return () => clearTimeout(timer);
  }, []);

  const getDirectionName = (heading) => {
    const directions = [
      'Utara', 'Utara-Timur', 'Timur', 'Tenggara',
      'Selatan', 'Barat Daya', 'Barat', 'Barat Laut'
    ];
    
    const index = Math.round(heading / 45) % 8;
    return directions[index];
  };

  const renderSimpleCompass = () => {
    const compassSize = width * 0.6;
    const centerX = compassSize / 2;
    const centerY = compassSize / 2;
    const needleLength = compassSize / 2 - 20;
    
    return (
      <View style={[styles.compassContainer, {width: compassSize, height: compassSize}]}>
        {/* Compass circle */}
        <View style={[styles.compassCircle, {width: compassSize, height: compassSize, borderRadius: compassSize / 2}]}>
          {/* North indicator */}
          <View style={[styles.northIndicator, {top: 10}]}>
            <Text style={styles.northText}>N</Text>
          </View>
          
          {/* East indicator */}
          <View style={[styles.eastIndicator, {right: 10}]}>
            <Text style={styles.directionText}>E</Text>
          </View>
          
          {/* South indicator */}
          <View style={[styles.southIndicator, {bottom: 10}]}>
            <Text style={styles.directionText}>S</Text>
          </View>
          
          {/* West indicator */}
          <View style={[styles.westIndicator, {left: 10}]}>
            <Text style={styles.directionText}>W</Text>
          </View>
          
          {/* Needle */}
          <View 
            style={[
              styles.needle,
              {
                transform: [{rotate: `${heading}deg`}],
                left: centerX - 2,
                top: centerY - needleLength,
                height: needleLength * 2,
              }
            ]}
          >
            <View style={styles.needlePoint} />
          </View>
          
          {/* Center dot */}
          <View style={[styles.centerDot, {left: centerX - 5, top: centerY - 5}]} />
        </View>
      </View>
    );
  };

  if (!isInitialized) {
    return (
      <SafeAreaView style={styles.container}>
        <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>Memuat Kompas...</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      
      <View style={styles.header}>
        <Text style={styles.headerText}>Kompas GPS</Text>
      </View>
      
      <View style={styles.content}>
        {renderSimpleCompass()}
        
        <View style={styles.infoContainer}>
          <Text style={styles.headingText}>
            {Math.round(heading)}°
          </Text>
          <Text style={styles.directionText}>
            {getDirectionName(heading)}
          </Text>
          
          <View style={styles.statusContainer}>
            <Text style={styles.statusText}>
              Mode Demo - Sensor tidak aktif
            </Text>
            <Text style={styles.instructionText}>
              Putar device untuk melihat perubahan arah
            </Text>
          </View>
        </View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 18,
    color: '#fff',
    textAlign: 'center',
  },
  header: {
    padding: 20,
    alignItems: 'center',
  },
  headerText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  compassContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 30,
  },
  compassCircle: {
    borderWidth: 3,
    borderColor: '#e94560',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    position: 'relative',
  },
  northIndicator: {
    position: 'absolute',
    alignItems: 'center',
  },
  eastIndicator: {
    position: 'absolute',
    alignItems: 'center',
    top: '50%',
    marginTop: -10,
  },
  southIndicator: {
    position: 'absolute',
    alignItems: 'center',
    left: '50%',
    marginLeft: -10,
  },
  westIndicator: {
    position: 'absolute',
    alignItems: 'center',
    top: '50%',
    marginTop: -10,
  },
  northText: {
    color: '#e94560',
    fontSize: 18,
    fontWeight: 'bold',
  },
  directionText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  needle: {
    position: 'absolute',
    width: 4,
    backgroundColor: '#e94560',
    borderRadius: 2,
  },
  needlePoint: {
    position: 'absolute',
    top: 0,
    left: -3,
    width: 0,
    height: 0,
    borderLeftWidth: 5,
    borderRightWidth: 5,
    borderBottomWidth: 15,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    borderBottomColor: '#e94560',
  },
  centerDot: {
    position: 'absolute',
    width: 10,
    height: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
  },
  infoContainer: {
    alignItems: 'center',
  },
  headingText: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
  },
  statusContainer: {
    marginTop: 20,
    padding: 15,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 10,
    alignItems: 'center',
  },
  statusText: {
    fontSize: 14,
    color: '#ffc107',
    marginBottom: 5,
    fontWeight: 'bold',
  },
  instructionText: {
    fontSize: 12,
    color: '#ccc',
    textAlign: 'center',
  },
});

export default AppMinimal;
