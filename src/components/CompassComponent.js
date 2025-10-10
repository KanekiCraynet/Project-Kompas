import React, {useRef, useEffect} from 'react';
import {
  View,
  StyleSheet,
  Dimensions,
  Animated,
} from 'react-native';
import Svg, {
  Circle,
  Line,
  Text as SvgText,
  G,
  Defs,
  LinearGradient,
  Stop,
} from 'react-native-svg';

const {width} = Dimensions.get('window');
const COMPASS_SIZE = width * 0.7;

const CompassComponent = ({heading, magnetometerData}) => {
  const rotateValue = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.timing(rotateValue, {
      toValue: -heading,
      duration: 200,
      useNativeDriver: true,
    }).start();
  }, [heading]);

  const renderCompassMarkers = () => {
    const markers = [];
    const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
    
    for (let i = 0; i < 8; i++) {
      const angle = i * 45;
      const x1 = COMPASS_SIZE / 2 + (COMPASS_SIZE / 2 - 20) * Math.cos((angle - 90) * Math.PI / 180);
      const y1 = COMPASS_SIZE / 2 + (COMPASS_SIZE / 2 - 20) * Math.sin((angle - 90) * Math.PI / 180);
      const x2 = COMPASS_SIZE / 2 + (COMPASS_SIZE / 2 - 10) * Math.cos((angle - 90) * Math.PI / 180);
      const y2 = COMPASS_SIZE / 2 + (COMPASS_SIZE / 2 - 10) * Math.sin((angle - 90) * Math.PI / 180);
      
      markers.push(
        <Line
          key={`marker-${i}`}
          x1={x1}
          y1={y1}
          x2={x2}
          y2={y2}
          stroke={i % 2 === 0 ? '#fff' : '#e94560'}
          strokeWidth={i % 2 === 0 ? 3 : 2}
        />
      );
      
      // Add direction labels
      const labelX = COMPASS_SIZE / 2 + (COMPASS_SIZE / 2 - 35) * Math.cos((angle - 90) * Math.PI / 180);
      const labelY = COMPASS_SIZE / 2 + (COMPASS_SIZE / 2 - 35) * Math.sin((angle - 90) * Math.PI / 180);
      
      markers.push(
        <SvgText
          key={`label-${i}`}
          x={labelX}
          y={labelY}
          fontSize="16"
          fill="#fff"
          textAnchor="middle"
          alignmentBaseline="middle"
        >
          {directions[i]}
        </SvgText>
      );
    }
    
    return markers;
  };

  const renderDegreeMarkers = () => {
    const markers = [];
    
    for (let i = 0; i < 36; i++) {
      const angle = i * 10;
      const isMainMarker = i % 3 === 0;
      const radius = isMainMarker ? COMPASS_SIZE / 2 - 15 : COMPASS_SIZE / 2 - 10;
      
      const x1 = COMPASS_SIZE / 2 + radius * Math.cos((angle - 90) * Math.PI / 180);
      const y1 = COMPASS_SIZE / 2 + radius * Math.sin((angle - 90) * Math.PI / 180);
      const x2 = COMPASS_SIZE / 2 + (radius + (isMainMarker ? 8 : 5)) * Math.cos((angle - 90) * Math.PI / 180);
      const y2 = COMPASS_SIZE / 2 + (radius + (isMainMarker ? 8 : 5)) * Math.sin((angle - 90) * Math.PI / 180);
      
      markers.push(
        <Line
          key={`degree-${i}`}
          x1={x1}
          y1={y1}
          x2={x2}
          y2={y2}
          stroke="#fff"
          strokeWidth={isMainMarker ? 2 : 1}
          opacity={0.7}
        />
      );
    }
    
    return markers;
  };

  const renderNeedle = () => {
    const centerX = COMPASS_SIZE / 2;
    const centerY = COMPASS_SIZE / 2;
    const needleLength = COMPASS_SIZE / 2 - 30;
    
    return (
      <G>
        {/* North needle (red) */}
        <Line
          x1={centerX}
          y1={centerY}
          x2={centerX}
          y2={centerY - needleLength}
          stroke="#e94560"
          strokeWidth="4"
          strokeLinecap="round"
        />
        
        {/* South needle (white) */}
        <Line
          x1={centerX}
          y1={centerY}
          x2={centerX}
          y2={centerY + needleLength}
          stroke="#fff"
          strokeWidth="4"
          strokeLinecap="round"
        />
        
        {/* Center dot */}
        <Circle
          cx={centerX}
          cy={centerY}
          r="8"
          fill="#1a1a2e"
          stroke="#fff"
          strokeWidth="2"
        />
      </G>
    );
  };

  return (
    <View style={styles.container}>
      <Animated.View
        style={[
          styles.compassContainer,
          {
            transform: [
              {
                rotate: rotateValue.interpolate({
                  inputRange: [0, 360],
                  outputRange: ['0deg', '360deg'],
                }),
              },
            ],
          },
        ]}
      >
        <Svg width={COMPASS_SIZE} height={COMPASS_SIZE}>
          <Defs>
            <LinearGradient id="compassGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <Stop offset="0%" stopColor="#16213e" stopOpacity="1" />
              <Stop offset="100%" stopColor="#0f3460" stopOpacity="1" />
            </LinearGradient>
          </Defs>
          
          {/* Compass background */}
          <Circle
            cx={COMPASS_SIZE / 2}
            cy={COMPASS_SIZE / 2}
            r={COMPASS_SIZE / 2 - 5}
            fill="url(#compassGradient)"
            stroke="#e94560"
            strokeWidth="3"
          />
          
          {/* Degree markers */}
          {renderDegreeMarkers()}
          
          {/* Direction markers */}
          {renderCompassMarkers()}
          
          {/* Needle */}
          {renderNeedle()}
        </Svg>
      </Animated.View>
      
      {/* Fixed direction indicator */}
      <View style={styles.directionIndicator}>
        <View style={styles.northIndicator}>
          <View style={styles.northArrow} />
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  compassContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  directionIndicator: {
    position: 'absolute',
    top: 10,
    alignItems: 'center',
  },
  northIndicator: {
    width: 0,
    height: 0,
    borderLeftWidth: 8,
    borderRightWidth: 8,
    borderBottomWidth: 15,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    borderBottomColor: '#e94560',
  },
  northArrow: {
    width: 0,
    height: 0,
    borderLeftWidth: 6,
    borderRightWidth: 6,
    borderBottomWidth: 12,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    borderBottomColor: '#fff',
    marginTop: -12,
  },
});

export default CompassComponent;
