// src/utils/distance.js

/**
 * Haversine 공식을 사용하여 두 지점 간의 거리(km) 계산
 * @param {number} lat1 위도1
 * @param {number} lon1 경도1
 * @param {number} lat2 위도2
 * @param {number} lon2 경도2
 * @returns {number} 거리 (km)
 */
export function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371 // 지구 반지름 (km)
  const dLat = deg2rad(lat2 - lat1)
  const dLon = deg2rad(lon2 - lon1)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const d = R * c // Distance in km
  return d
}

function deg2rad(deg) {
  return deg * (Math.PI / 180)
}
