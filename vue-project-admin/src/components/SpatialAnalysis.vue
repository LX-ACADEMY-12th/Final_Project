<template>
    <div class="card shadow-sm h-100 d-flex flex-column">
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

        <div class="card-body p-0 d-flex flex-row flex-grow-1">
            <div id="path-analysis-map" style="height: 100%; width: 66.6%;"></div>

            <div class="p-3 border-start d-flex flex-column" style="width: 33.3%;">
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

                <h6 class="fw-bold text-secondary mt-1 mb-2">동선 상세 목록</h6>
                <div class="list-group list-group-flush small flex-grow-1" style="overflow-y: auto;">
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
            markerLayerGroup: null,
            legendControl: null,
            startDate: '2025-10-01',
            endDate: getTodayDate(),
            analysisType: 'PLACE',
            pathKeyMap: new Map(),
            analysisStats: {
                segmentCount: 0,
                totalDays: 0,
                topPath: { name: '', count: 0, startLat: 0, startLng: 0, endLat: 0, endLng: 0 },
                detailedPaths: []
            },
            // 💡 ResizeObserver 인스턴스를 저장하기 위한 변수 추가
            resizeObserver: null,
        };
    },
    mounted() {
        this.$nextTick(this.initMap);
        this.updateAnalysis();
    },
    watch: {
        spatialData: {
            handler() {
                if (this.map) {
                    this.calculateStats();
                    this.drawPaths();
                }
            },
            deep: true,
        },
    },
    methods: {
        calculateStats() {
            if (!Array.isArray(this.spatialData) || this.spatialData.length === 0) {
                this.analysisStats = { segmentCount: 0, totalDays: 0, topPath: { name: '데이터 없음', count: 0 }, detailedPaths: [] };
                return;
            }

            const totalSegments = this.spatialData.reduce((sum, s) => sum + s.segmentCount, 0);

            const start = new Date(this.startDate);
            const end = new Date(this.endDate);
            const diffTime = Math.abs(end - start);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

            const detailedPaths = this.spatialData.map(s => ({
                count: s.segmentCount,
                startName: s.startPlaceName || `장소 ${s.startPlaceId}`,
                endName: s.endPlaceName || `장소 ${s.endPlaceId}`,
                startLat: s.startLat,
                startLng: s.startLng,
                endLat: s.endLat,
                endLng: s.endLng,
                type: (s.segmentCount >= 50) ? '최다 이동' : (s.segmentCount >= 10 ? '주요 이동' : '일반 이동')
            }));

            const top = detailedPaths[0] || { count: 0, startName: '데이터 없음', endName: '', startLat: 0, startLng: 0, endLat: 0, endLng: 0 };
            const topPathName = `${top.startName} → ${top.endName}`;

            this.analysisStats = {
                segmentCount: totalSegments,
                totalDays: diffDays,
                topPath: { name: topPathName, count: top.count, startLat: top.startLat, startLng: top.startLng, endLat: top.endLat, endLng: top.endLng },
                detailedPaths: detailedPaths
            };
        },

        zoomToPath(startLat, startLng, endLat, endLng) {
            if (!this.map) return;
            const startPoint = L.latLng(startLat, startLng);
            const endPoint = L.latLng(endLat, endLng);

            this.map.fitBounds(L.latLngBounds(startPoint, endPoint), { padding: [100, 100] });
        },

        updateAnalysis() {
            this.$emit('reload-data', this.startDate, this.endDate, this.analysisType);

            // ✅ 데이터 로드 후 지도 갱신 대기
            this.$nextTick(() => {
                setTimeout(() => {
                    if (this.map) {
                        this.map.invalidateSize();
                    }
                }, 150);
            });
        },

        initMap() {
            this.map = L.map('path-analysis-map').setView([36.5, 127.8], 7);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
            }).addTo(this.map);
            this.pathLayerGroup = L.layerGroup().addTo(this.map);
            this.markerLayerGroup = L.layerGroup().addTo(this.map);
            this.drawPaths();

            // 💡 [수정] 맵 크기 갱신 이벤트 핸들러 추가 및 로딩 직후 강제 갱신
            this.resizeObserver = new ResizeObserver(() => {
                if (this.map) {
                    this.map.invalidateSize();
                }
            });
            const mapContainer = document.getElementById('path-analysis-map');
            if (mapContainer) {
                this.resizeObserver.observe(mapContainer);
            }

            // 줌 변경 후에도 크기를 갱신하여 핀/선 어긋남 재확인
            this.map.on('moveend', () => {
                this.map.invalidateSize();
            });

            // 초기 로딩 후 잠깐의 딜레이를 주어 크기 갱신 (가장 확실한 방법)
            setTimeout(() => {
                if (this.map) this.map.invalidateSize();
            }, 100);
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
        getLineOpacity(count) {
            if (count >= 50) return 1.0;
            if (count >= 10) return 0.8;
            return 0.5;
        },

        addLegend() {
            const legend = L.control({ position: 'bottomright' });
            legend.onAdd = () => {
                const div = L.DomUtil.create('div', 'info legend');
                div.style.backgroundColor = 'white';
                div.style.padding = '10px';
                div.style.borderRadius = '5px';
                div.innerHTML = '<b>경로 이동 빈도</b><br>' +
                    '<i style="background:#FF5733; width: 20px; height: 8px; display: inline-block; margin-right: 5px;"></i> 50회 이상 (매우 잦음)<br>' +
                    '<i style="background:#4A7CEC; width: 20px; height: 5px; display: inline-block; margin-right: 5px;"></i> 10회 ~ 49회 (잦음)<br>' +
                    '<i style="background:#00BFFF; width: 20px; height: 3px; display: inline-block; margin-right: 5px;"></i> 1회 ~ 9회 (낮음)';
                return div;
            };

            if (this.legendControl) { this.map.removeControl(this.legendControl); }
            this.legendControl = legend;
            legend.addTo(this.map);
        },

        drawMarkers() {
            if (this.markerLayerGroup) { this.map.removeLayer(this.markerLayerGroup); }
            this.markerLayerGroup = L.layerGroup().addTo(this.map);

            const places = {};
            this.spatialData.forEach(segment => {
                if (segment.startLat && !places[segment.startPlaceId]) {
                    places[segment.startPlaceId] = { name: segment.startPlaceName || `장소 ${segment.startPlaceId}`, lat: segment.startLat, lng: segment.startLng, count: 0 };
                }
                if (segment.endLat && !places[segment.endPlaceId]) {
                    places[segment.endPlaceId] = { name: segment.endPlaceName || `장소 ${segment.endPlaceId}`, lat: segment.endLat, lng: segment.endLng, count: 0 };
                }
                if (places[segment.startPlaceId]) places[segment.startPlaceId].count += segment.segmentCount;
                if (places[segment.endPlaceId]) places[segment.endPlaceId].count += segment.segmentCount;
            });

            Object.keys(places).forEach(id => {
                const place = places[id];
                const markerSize = 25 + Math.min(place.count / 10, 20);

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
            this.pathKeyMap.clear();

            this.drawMarkers();
            this.addLegend();

            if (!Array.isArray(this.spatialData) || this.spatialData.length === 0) {
                this.map.setView([36.5, 127.8], 7);
                return;
            }

            const allPathCoordinates = [];
            const offsetDistance = 0.00008;

            this.spatialData.forEach(segment => {
                try {
                    const geoJson = JSON.parse(segment.pathGeoJson);
                    if (geoJson && geoJson.type === 'LineString') {
                        const count = segment.segmentCount;
                        const weight = this.getLineWeight(count);
                        const color = this.getLineColor(count);
                        const opacity = this.getLineOpacity(count);

                        const startId = segment.startPlaceId;
                        const endId = segment.endPlaceId;

                        const forwardKey = `${startId}-${endId}`;
                        const backwardKey = `${endId}-${startId}`;

                        let offsetGeoJson = geoJson;
                        let isOverlapping = false;

                        if (this.pathKeyMap.has(backwardKey)) {
                            offsetGeoJson = this.applyOffsetToGeoJson(geoJson, offsetDistance);
                            isOverlapping = true;
                        }

                        if (!this.pathKeyMap.has(forwardKey)) {
                            this.pathKeyMap.set(forwardKey, true);
                        } else {
                            return;
                        }

                        const pathLayer = L.geoJSON(offsetGeoJson, {
                            style: {
                                color: color,
                                weight: weight,
                                opacity: opacity,
                                lineCap: 'round',
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

            // ✅ 추가: 경로 그리기 완료 후 지도 크기 갱신
            this.$nextTick(() => {
                setTimeout(() => {
                    if (this.map) {
                        this.map.invalidateSize();
                    }
                }, 100);
            });
        },

        applyOffsetToGeoJson(geoJson, offset) {
            if (geoJson.type !== 'LineString' || offset === 0) return geoJson;
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
            // 💡 [추가] ResizeObserver가 있다면 해제
            if (this.resizeObserver) {
                const mapContainer = document.getElementById('path-analysis-map');
                if (mapContainer) this.resizeObserver.unobserve(mapContainer);
            }
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
    /* 💡 [확인] 마커가 중앙에 정확히 위치하도록 transform 설정 */
    transform: translate(-50%, -50%);
}

/* UI 레이아웃 */
.card {
    display: flex;
    flex-direction: column;
}

.card-body {
    flex-grow: 1;
    display: flex;
    flex-direction: row;
    min-height: 0;
}

#path-analysis-map {
    min-height: 500px;
}

.p-3.border-start {
    display: flex;
    flex-direction: column;
}

.list-group {
    flex-grow: 1;
    overflow-y: auto;
}
</style>