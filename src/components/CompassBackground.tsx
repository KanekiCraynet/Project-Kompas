import React from 'react';
import { View, StyleSheet } from 'react-native';
import Svg, { Circle, Text as SvgText, Line } from 'react-native-svg';

interface CompassBackgroundProps {
  size: number;
}

const CompassBackground: React.FC<CompassBackgroundProps> = ({ size }) => {
  const center = size / 2;
  const radius = size * 0.4;
  const innerRadius = size * 0.1;

  // Generate degree markers
  const degreeMarkers = [];
  const directionLabels = ['N', 'E', 'S', 'W'];
  const directionAngles = [0, 90, 180, 270];

  // Create degree markers every 30 degrees
  for (let i = 0; i < 360; i += 30) {
    const angle = (i * Math.PI) / 180;
    const isMajor = i % 90 === 0;
    const markerRadius = isMajor ? radius - size * 0.05 : radius - size * 0.03;
    const markerLength = isMajor ? size * 0.05 : size * 0.03;
    
    const x1 = center + markerRadius * Math.sin(angle);
    const y1 = center - markerRadius * Math.cos(angle);
    const x2 = center + (markerRadius + markerLength) * Math.sin(angle);
    const y2 = center - (markerRadius + markerLength) * Math.cos(angle);

    degreeMarkers.push(
      <Line
        key={i}
        x1={x1}
        y1={y1}
        x2={x2}
        y2={y2}
        stroke={isMajor ? '#4ecdc4' : '#666'}
        strokeWidth={isMajor ? 3 : 2}
        strokeLinecap="round"
      />
    );

    // Add direction labels for major markers
    if (isMajor) {
      const labelIndex = directionAngles.indexOf(i);
      if (labelIndex !== -1) {
        const labelX = center + (radius + size * 0.08) * Math.sin(angle);
        const labelY = center - (radius + size * 0.08) * Math.cos(angle);
        
        degreeMarkers.push(
          <SvgText
            key={`label-${i}`}
            x={labelX}
            y={labelY}
            fontSize={size * 0.08}
            fill="#4ecdc4"
            textAnchor="middle"
            alignmentBaseline="middle"
            fontWeight="bold"
          >
            {directionLabels[labelIndex]}
          </SvgText>
        );
      }
    }
  }

  // Create minor degree markers every 10 degrees
  const minorMarkers = [];
  for (let i = 0; i < 360; i += 10) {
    if (i % 30 === 0) continue; // Skip major markers
    
    const angle = (i * Math.PI) / 180;
    const markerRadius = radius - size * 0.025;
    const markerLength = size * 0.02;
    
    const x1 = center + markerRadius * Math.sin(angle);
    const y1 = center - markerRadius * Math.cos(angle);
    const x2 = center + (markerRadius + markerLength) * Math.sin(angle);
    const y2 = center - (markerRadius + markerLength) * Math.cos(angle);

    minorMarkers.push(
      <Line
        key={`minor-${i}`}
        x1={x1}
        y1={y1}
        x2={x2}
        y2={y2}
        stroke="#888"
        strokeWidth={1}
        strokeLinecap="round"
      />
    );
  }

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      <Svg width={size} height={size}>
        {/* Outer circle */}
        <Circle
          cx={center}
          cy={center}
          r={radius}
          fill="none"
          stroke="#4ecdc4"
          strokeWidth={3}
        />
        
        {/* Inner circle */}
        <Circle
          cx={center}
          cy={center}
          r={innerRadius}
          fill="none"
          stroke="#4ecdc4"
          strokeWidth={2}
        />
        
        {/* Center dot */}
        <Circle
          cx={center}
          cy={center}
          r={size * 0.01}
          fill="#4ecdc4"
        />
        
        {/* Degree markers */}
        {degreeMarkers}
        {minorMarkers}
        
        {/* Cardinal direction lines */}
        {directionAngles.map((angle, index) => {
          const angleRad = (angle * Math.PI) / 180;
          const x1 = center + innerRadius * Math.sin(angleRad);
          const y1 = center - innerRadius * Math.cos(angleRad);
          const x2 = center + (radius - size * 0.05) * Math.sin(angleRad);
          const y2 = center - (radius - size * 0.05) * Math.cos(angleRad);
          
          return (
            <Line
              key={`cardinal-${angle}`}
              x1={x1}
              y1={y1}
              x2={x2}
              y2={y2}
              stroke="#4ecdc4"
              strokeWidth={2}
              strokeLinecap="round"
            />
          );
        })}
      </Svg>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default CompassBackground;
