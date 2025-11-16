<template>
    <div class="card shadow-sm h-100">
        <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0"><i class="bi bi-bar-chart-fill me-2"></i>사용자 동선 분석 (Path Analysis)</h5>
            <div class="d-flex align-items-center">
                <div class="btn-group btn-group-sm me-4" role="group">
                    <input type="radio" class="btn-check" id="type-place" value="PLACE" v-model="analysisType"
                        @change="updateAnalysis">
                    <label class="btn btn-outline-primary" for="type-place">장소 간 동선</label>

                    <input type="radio" class="btn-check" id="type-exhibit" value="EXHIBITION" v-model="analysisType"
                        @change="updateAnalysis">
                    <label class="btn btn-outline-primary" for="type-exhibit">전시 간 동선</label>
                </div>
                <label for="startDate" class="form-label mb-0 me-2 small">기간:</label>
                <input type="date" id="startDate" class="form-control form-control-sm me-2" v-model="startDate"
                    @change="updateAnalysis" style="width: 140px;">
                <span class="me-2 small">~</span>
                <input type="date" id="endDate" class="form-control form-control-sm" v-model="endDate"
                    @change="updateAnalysis" style="width: 140px;">
            </div>
        </div>

        <div class="card-body p-0 d-flex flex-row">
            <div id="path-analysis-map" style="height: 650px; width: 66.6%;"></div>

            <div class="p-3 border-start" style="width: 33.3%;">
                <h6 class="fw-bold text-primary mb-3">분석 요약 (총 {{ analysisStats.segmentCount }}회 이동)</h6>

                <div class="bg-light p-3 rounded mb-3">
                    <p class="mb-1 small text-muted">최다 발생 동선 (Top 1)</p>
                    <h5 class="fw-bold text-dark mb-0 text-truncate">
                        <i class="bi bi-arrow-right-circle-fill me-1"></i>
                        {{ analysisStats.topPath.name || '데이터 없음' }}
                    </h5>
                    <span class="badge bg-primary mt-1">{{ analysisStats.topPath.count }}회 이동</span>
                </div>

                <div class="bg-light p-3 rounded mb-3">
                    <p class="mb-1 small text-muted">분석 기간</p>
                    <h5 class="fw-bold text-dark mb-0">{{ analysisStats.totalDays }}일 간 분석</h5>
                </div>

                <h6 class="fw-bold text-secondary mt-4 mb-2">동선 상세 목록</h6>
                <div class="list-group list-group-flush small" style="max-height: 400px; overflow-y: auto;">
                    <a href="#" class="list-group-item list-group-item-action py-2"
                        v-for="(path, index) in analysisStats.detailedPaths" :key="index"
                        @click.prevent="zoomToPath(path.startLat, path.startLng, path.endLat, path.endLng)">
                        <div class="d-flex w-100 justify-content-between">
                            <small :class="{ 'fw-bold text-danger': index === 0 }">{{ path.count }}회</small>
                            <small class="text-muted">{{ path.type }}</small>
                        </div>
                        <p class="mb-1 text-truncate" :class="{ 'fw-bold': index === 0 }">
                            {{ path.startName }} <i class="bi bi-caret-right-fill mx-1"></i> {{ path.endName }}
                        </p>
                    </a>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Leaflet 마커 이미지 경로 수정 (필수)
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

L.Icon.Default.mergeOptions({ iconRetinaUrl: markerIcon2x, iconUrl: markerIcon, shadowUrl: markerShadow });
// 현재 날짜를 'YYYY-MM-DD' 형식의 문자열로 반환하는 헬퍼 함수
function getTodayDate() {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const dd = String(today.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}
export default {
    name: "SpatialAnalysis",
    props: {
        // 백엔드에서 받은 List<UserPathSegmentDto> 데이터 (이름 및 좌표 포함)
        spatialData: {
            type: Array,
            required: true,
            default: () => [],
        },
    },
    data() {
        return {
            map: null,
            pathLayerGroup: null,
            markerLayerGroup: null,    // 💡 마커 레이어 그룹
            legendControl: null,       // 💡 범례 컨트롤 객체
            startDate: '2025-10-01',
            endDate: getTodayDate(),
            analysisType: 'PLACE', // 💡 기본값을 'PLACE'로 설정
            pathKeyMap: new Map(), // 💡 A->B 경로를 저장하여 B->A 경로와 겹침을 확인
            // 💡 통계 데이터를 위한 객체
            analysisStats: {
                segmentCount: 0,
                totalDays: 0,
                topPath: { name: '', count: 0, startLat: 0, startLng: 0, endLat: 0, endLng: 0 },
                detailedPaths: []
            }
        };
    },
    mounted() {
        this.$nextTick(this.initMap);
        // 초기 맵 로딩 후, 설정된 초기 날짜/타입으로 데이터 로드를 요청
        this.updateAnalysis();
    },
    watch: {
        spatialData: {
            handler() {
                if (this.map) {
                    this.calculateStats(); // 통계 먼저 계산
                    this.drawPaths();
                }
            },
            deep: true,
        },
    },
    methods: {
        // 💡 통계 패널 데이터를 계산하는 로직
        calculateStats() {
            // 🚨 [수정된 부분] this.spatialData가 배열인지 확인하는 로직 추가
            if (!Array.isArray(this.spatialData) || this.spatialData.length === 0) {
                console.warn("calculateStats: spatialData가 유효한 배열이 아니거나 비어있습니다.");
                this.analysisStats = { segmentCount: 0, totalDays: 0, topPath: { name: '데이터 없음', count: 0 }, detailedPaths: [] };
                return;
            }

            // 오류가 발생했던 reduce 호출 부분은 이제 안전하게 실행됩니다.
            const totalSegments = this.spatialData.reduce((sum, s) => sum + s.segmentCount, 0);

            // 날짜 차이 계산
            const start = new Date(this.startDate);
            const end = new Date(this.endDate);
            const diffTime = Math.abs(end - start);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

            // 상세 경로 목록 생성 (이름이 없는 경우 ID 사용)
            const detailedPaths = this.spatialData.map(s => ({
                count: s.segmentCount,
                startName: s.startPlaceName || `장소 ${s.startPlaceId}`, // 💡 DTO의 이름 필드 사용
                endName: s.endPlaceName || `장소 ${s.endPlaceId}`,       // 💡 DTO의 이름 필드 사용
                startLat: s.startLat,
                startLng: s.startLng,
                endLat: s.endLat,
                endLng: s.endLng,
                type: (s.segmentCount >= 50) ? '최다 이동' : (s.segmentCount >= 10 ? '주요 이동' : '일반 이동')
            }));

            // 최다 이동 경로
            const top = detailedPaths[0];
            const topPathName = `${top.startName} → ${top.endName}`;

            this.analysisStats = {
                segmentCount: totalSegments,
                totalDays: diffDays,
                topPath: { name: topPathName, count: top.count, startLat: top.startLat, startLng: top.startLng, endLat: top.endLat, endLng: top.endLng },
                detailedPaths: detailedPaths
            };
        },

        // 💡 [신규] 특정 경로로 지도 뷰 이동 (통계 패널 클릭 시 사용)
        zoomToPath(startLat, startLng, endLat, endLng) {
            if (!this.map) return;
            const startPoint = L.latLng(startLat, startLng);
            const endPoint = L.latLng(endLat, endLng);

            this.map.fitBounds(L.latLngBounds(startPoint, endPoint), { padding: [100, 100] });
        },

        updateAnalysis() {
            this.$emit('reload-data', this.startDate, this.endDate, this.analysisType);
        },

        initMap() {
            this.map = L.map('path-analysis-map').setView([36.5, 127.8], 7);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
            }).addTo(this.map);
            this.pathLayerGroup = L.layerGroup().addTo(this.map);
            this.markerLayerGroup = L.layerGroup().addTo(this.map); // 💡 마커 레이어 그룹 초기화
            this.drawPaths();
        },

        getLineWeight(count) {
            if (count >= 50) return 8;
            if (count >= 20) return 6;
            if (count >= 5) return 4;
            return 2;
        },
        getLineColor(count) {
            if (count >= 50) return '#FF5733';
            if (count >= 10) return '#4A7CEC';
            return '#00BFFF';
        },

        // 💡 [통합] 범례 추가 함수
        addLegend() {
            const legend = L.control({ position: 'bottomright' });
            legend.onAdd = () => {
                const div = L.DomUtil.create('div', 'info legend');
                div.style.backgroundColor = 'white';
                div.style.padding = '10px';
                div.style.borderRadius = '5px';
                div.innerHTML = '<b>경로 이동 빈도</b><br>' +
                    '<i style="background:#FF5733; width: 20px; height: 8px; display: inline-block; margin-right: 5px;"></i> 50회 이상 (매우 잦음)<br>' +
                    '<i style="background:#4A7CEC; width: 20px; height: 5px; display: inline-block; margin-right: 5px;"></i> 10회 ~ 49회 (중간)<br>' +
                    '<i style="background:#00BFFF; width: 20px; height: 3px; display: inline-block; margin-right: 5px;"></i> 1회 ~ 9회 (낮음)';
                return div;
            };

            if (this.legendControl) { this.map.removeControl(this.legendControl); }
            this.legendControl = legend;
            legend.addTo(this.map);
        },

        // 💡 [통합] 장소 마커를 지도에 표시하는 함수
        drawMarkers() {
            if (this.markerLayerGroup) { this.map.removeLayer(this.markerLayerGroup); }
            this.markerLayerGroup = L.layerGroup().addTo(this.map);

            const places = {};
            this.spatialData.forEach(segment => {
                // GeoJSON 파싱을 피하기 위해 DTO에 좌표와 이름이 포함되어 있어야 합니다.

                // start point (이름과 좌표를 DTO에서 가져와서 마커를 생성)
                if (segment.startLat && !places[segment.startPlaceId]) {
                    places[segment.startPlaceId] = { name: segment.startPlaceName || `장소 ${segment.startPlaceId}`, lat: segment.startLat, lng: segment.startLng, count: 0 };
                }
                // end point
                if (segment.endLat && !places[segment.endPlaceId]) {
                    places[segment.endPlaceId] = { name: segment.endPlaceName || `장소 ${segment.endPlaceId}`, lat: segment.endLat, lng: segment.endLng, count: 0 };
                }
                // 마커 크기 계산을 위해 총 이동 카운트 누적
                if (places[segment.startPlaceId]) places[segment.startPlaceId].count += segment.segmentCount;
                if (places[segment.endPlaceId]) places[segment.endPlaceId].count += segment.segmentCount;
            });

            // 각 장소에 마커 생성
            Object.keys(places).forEach(id => {
                const place = places[id];
                const markerSize = 25 + Math.min(place.count / 10, 20); // 이동량이 많을수록 마커 크기 증가 (최대 45px)

                const customIcon = L.divIcon({
                    className: 'custom-div-icon',
                    html: `<div style="background-color: #34495e; border-radius: 50%; width: ${markerSize}px; height: ${markerSize}px; text-align: center; line-height: ${markerSize}px; color: white; font-weight: bold; font-size: 10px; border: 2px solid white;">${place.name.substring(0, 1)}</div>`,
                    iconSize: [markerSize, markerSize],
                });

                L.marker([place.lat, place.lng], { icon: customIcon })
                    .bindPopup(`<b>${place.name}</b><br>총 이동량(출/도착): ${place.count}회`)
                    .addTo(this.markerLayerGroup);
            });
        },


        drawPaths() {
            if (!this.map) return;
            this.pathLayerGroup.clearLayers();
            this.pathKeyMap.clear(); // 경로 그릴 때마다 맵 초기화

            // 💡 [통합] 마커와 범례를 먼저 그립니다.
            this.drawMarkers();
            this.addLegend();

            // 🚨 [추가된 부분] drawPaths에서도 배열 유효성 확인
            if (!Array.isArray(this.spatialData) || this.spatialData.length === 0) {
                console.warn("표시할 동선 세그먼트 데이터가 없습니다.");
                this.map.setView([36.5, 127.8], 7);
                return;
            }

            const allPathCoordinates = [];

            this.spatialData.forEach(segment => {
                try {
                    const geoJson = JSON.parse(segment.pathGeoJson);
                    if (geoJson && geoJson.type === 'LineString') {
                        const count = segment.segmentCount;
                        const weight = this.getLineWeight(count);
                        const color = this.getLineColor(count);

                        // --- 💡 [수정] 경로 겹침 확인 및 오프셋 적용 로직 시작 ---
                        const startId = segment.startPlaceId;
                        const endId = segment.endPlaceId;

                        // 정규화된 키: ID가 작은 순서대로 정렬 (예: 1-5 또는 5-1)
                        const forwardKey = `${startId}-${endId}`;
                        const backwardKey = `${endId}-${startId}`;

                        let isOverlapping = false;
                        let offsetValue = 0; // 오프셋 값 (픽셀 단위로 선을 밀어냄)
                        const baseOffset = 0.00005; // 좌표계를 미세하게 이동시킬 기본 값 (작게 설정)

                        if (this.pathKeyMap.has(backwardKey)) {
                            // 역방향 경로(B->A)가 이미 그려진 경우 (A->B 경로가 먼저 그려짐)
                            isOverlapping = true;
                            offsetValue = baseOffset; // A->B가 그려졌으므로, B->A를 오프셋
                        }

                        // 현재 경로를 pathKeyMap에 추가 (겹침 체크를 위해)
                        if (!this.pathKeyMap.has(forwardKey)) {
                            this.pathKeyMap.set(forwardKey, true);
                        }
                        // --- 💡 [수정] 경로 겹침 확인 및 오프셋 적용 로직 끝 ---

                        // GeoJSON 좌표에 오프셋 적용
                        let offsetGeoJson = geoJson;
                        if (isOverlapping) {
                            offsetGeoJson = this.applyOffsetToGeoJson(geoJson, offsetValue);
                        }

                        const pathLayer = L.geoJSON(offsetGeoJson, {
                            // isOverlapping일 때 스타일을 살짝 다르게 하면 시각적으로 더 잘 구별됩니다.
                            style: {
                                color: color,
                                weight: isOverlapping ? weight + 1 : weight, // 겹칠 때 두께를 살짝 굵게
                                opacity: 0.8,
                                lineCap: 'round',
                                // 💡 (주의) Leaflet에는 기본 오프셋 기능이 없으므로, GeoJSON 데이터를 직접 수정해야 합니다. 
                                // 아래의 applyOffsetToGeoJson 메서드를 정의해야 합니다.
                            }
                        }).bindPopup(
                            `<b>동선 분석 결과</b><hr>` +
                            `출발: <b>${segment.startPlaceName || segment.startPlaceId}</b><br>` +
                            `도착: <b>${segment.endPlaceName || segment.endPlaceId}</b><br>` +
                            `이동 빈도: <b>${count}회</b>` +
                            (isOverlapping ? `<br><small style="color: red;">(경로 겹침: 오프셋 적용됨)</small>` : '')
                        );

                        this.pathLayerGroup.addLayer(pathLayer);

                        L.geoJSON(geoJson).getLayers().forEach(layer => {
                            if (layer.getLatLngs) { allPathCoordinates.push(...layer.getLatLngs()); }
                        });
                    }
                } catch (e) {
                    console.error("GeoJSON 데이터 파싱 오류:", e, segment.pathGeoJson);
                }
            });

            if (allPathCoordinates.length > 0) {
                this.map.fitBounds(allPathCoordinates, { padding: [50, 50] });
            }
        },
        // 3. 오프셋 적용 헬퍼 메서드 추가
        // 경로를 그리는 방향에 수직하게 좌표를 미세하게 이동시킵니다.
        applyOffsetToGeoJson(geoJson, offset) {
            if (geoJson.type !== 'LineString' || offset === 0) return geoJson;

            const newCoordinates = geoJson.coordinates.map((coord, index) => {
                if (index === 0) {
                    // 첫 번째 좌표는 다음 좌표로 향하는 벡터의 수직 벡터를 사용
                    if (geoJson.coordinates.length > 1) {
                        const nextCoord = geoJson.coordinates[index + 1];
                        const dx = nextCoord[0] - coord[0];
                        const dy = nextCoord[1] - coord[1];
                        // 수직 벡터를 이용해 오프셋 적용 (간단화)
                        return [coord[0] + offset * dy, coord[1] - offset * dx];
                    }
                    return coord;
                } else if (index === geoJson.coordinates.length - 1) {
                    // 마지막 좌표는 이전 좌표에서 오는 벡터의 수직 벡터를 사용
                    const prevCoord = geoJson.coordinates[index - 1];
                    const dx = coord[0] - prevCoord[0];
                    const dy = coord[1] - prevCoord[1];
                    return [coord[0] - offset * dy, coord[1] + offset * dx];
                } else {
                    // 중간 좌표는 앞뒤 좌표의 평균 벡터를 사용해야 하지만, 여기서는 간단하게 경도(Lng)만 오프셋
                    return [coord[0] + offset, coord[1]];
                }
            });

            // LineString을 Path로 변환할 때 문제가 발생할 수 있으므로, 단순화를 위해 경도(Lng)만 이동시키는 방법을 사용하겠습니다.
            // 이는 지리적으로 정확한 오프셋은 아니지만, 시각적인 분리에는 효과적입니다.
            const simplifiedCoordinates = geoJson.coordinates.map(coord => [coord[0] + offset, coord[1]]);

            return {
                ...geoJson,
                coordinates: simplifiedCoordinates
            };
        },
    },
    emits: ['reload-data'],
    beforeUnmount() {
        if (this.map) {
            this.map.remove();
        }
    }
};
</script>

<style scoped>
/* Leaflet DivIcon의 커스텀 스타일 */
.custom-div-icon {
    border-radius: 50%;
    text-align: center;
    font-size: 10px;
    font-weight: bold;
    color: white;
    box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
}

/* 지도와 통계 패널의 높이를 일정하게 유지 */
.card-body {
    display: flex;
    flex-direction: row;
}

/* 지도 컨테이너 자체의 높이를 고정 (h-100 클래스는 부모 요소 높이에 따라 달라짐) */
#path-analysis-map {
    min-height: 650px;
}
</style>