<template>
    <div class="card shadow-sm h-100 d-flex flex-column">
        <!-- 헤더 -->
        <div class="card-header bg-white d-flex justify-content-between align-items-center flex-wrap gap-2">
            <h5 class="mb-0">
                <i class="bi bi-fire me-2 text-danger"></i>핫스팟 분석 (Hotspot Analysis)
            </h5>
            <div class="d-flex align-items-center flex-wrap gap-2">
                <!-- 기간 선택 -->
                <label class="form-label mb-0 me-2 small">기간:</label>
                <input type="date" class="form-control form-control-sm" v-model="startDate" @change="fetchAnalysis"
                    :max="endDate" style="width: 140px;">
                <span class="mx-1 small">~</span>
                <input type="date" class="form-control form-control-sm" v-model="endDate" @change="fetchAnalysis"
                    :min="startDate" style="width: 140px;">
                <button class="btn btn-primary btn-sm ms-2" @click="fetchAnalysis">
                    <i class="bi bi-search"></i> 분석
                </button>
            </div>
        </div>

        <!-- 바디 -->
        <div class="card-body p-0 d-flex flex-row flex-grow-1" style="min-height: 600px;">
            <!-- 좌측: 지도 (히트맵) -->
            <div class="position-relative" style="width: 55%;">
                <div id="hotspot-map" style="height: 100%; width: 100%;"></div>
                <!-- 로딩 오버레이 -->
                <div v-if="loading"
                    class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
                    style="background: rgba(255,255,255,0.8); z-index: 1000;">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>

            <!-- 우측: 분석 패널 -->
            <div class="border-start d-flex flex-column" style="width: 45%;">
                <!-- 탭 네비게이션 -->
                <ul class="nav nav-tabs px-3 pt-2">
                    <li class="nav-item">
                        <a class="nav-link" :class="{ active: activeTab === 'ranking' }" href="#"
                            @click.prevent="activeTab = 'ranking'">
                            <i class="bi bi-trophy me-1"></i>핫스팟 랭킹
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" :class="{ active: activeTab === 'trend' }" href="#"
                            @click.prevent="activeTab = 'trend'">
                            <i class="bi bi-graph-up me-1"></i>트렌드
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" :class="{ active: activeTab === 'pattern' }" href="#"
                            @click.prevent="activeTab = 'pattern'">
                            <i class="bi bi-calendar-week me-1"></i>패턴 분석
                        </a>
                    </li>
                </ul>

                <!-- 탭 컨텐츠 -->
                <div class="tab-content flex-grow-1 overflow-auto p-3">
                    <!-- 핫스팟 랭킹 탭 -->
                    <div v-if="activeTab === 'ranking'">
                        <!-- 요약 통계 -->
                        <div class="row g-2 mb-3">
                            <div class="col-6">
                                <div class="bg-primary bg-opacity-10 rounded p-2 text-center">
                                    <div class="small text-muted">총 방문</div>
                                    <div class="fs-5 fw-bold text-primary">{{ formatNumber(analysisData.totalVisits) }}
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="bg-success bg-opacity-10 rounded p-2 text-center">
                                    <div class="small text-muted">고유 방문자</div>
                                    <div class="fs-5 fw-bold text-success">{{
                                        formatNumber(analysisData.totalUniqueUsers) }}</div>
                                </div>
                            </div>
                        </div>

                        <!-- 핫스팟 TOP 10 -->
                        <h6 class="fw-bold mb-2"><i class="bi bi-fire text-danger me-1"></i>TOP 10 핫스팟</h6>
                        <div class="list-group list-group-flush mb-3">
                            <a href="#" class="list-group-item list-group-item-action py-2"
                                v-for="(place, index) in analysisData.hotspotRanking" :key="place.placeId"
                                @click.prevent="selectPlace(place)"
                                :class="{ 'active': selectedPlace?.placeId === place.placeId }">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <span class="badge me-2" :class="getRankBadgeClass(index)">{{ index + 1
                                        }}</span>
                                        <span :class="{ 'fw-bold': index < 3 }">{{ place.placeName }}</span>
                                    </div>
                                    <div class="text-end">
                                        <span class="badge bg-primary">{{ place.visitCount }}회</span>
                                        <small class="text-muted ms-1">({{ place.uniqueUserCount }}명)</small>
                                    </div>
                                </div>
                            </a>
                        </div>

                        <!-- 급상승 장소 -->
                        <h6 class="fw-bold mb-2"><i class="bi bi-arrow-up-circle text-success me-1"></i>급상승 장소</h6>
                        <div class="list-group list-group-flush">
                            <div class="list-group-item py-2" v-for="place in analysisData.risingPlaces?.slice(0, 5)"
                                :key="'rising-' + place.placeId">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span>{{ place.placeName }}</span>
                                    <span class="badge" :class="getTrendBadgeClass(place.trendStatus)">
                                        <i class="bi" :class="getTrendIcon(place.trendStatus)"></i>
                                        {{ place.growthRate > 0 ? '+' : '' }}{{ place.growthRate }}%
                                    </span>
                                </div>
                            </div>
                            <div v-if="!analysisData.risingPlaces?.length" class="text-muted small text-center py-3">
                                급상승 장소가 없습니다
                            </div>
                        </div>
                    </div>

                    <!-- 트렌드 탭 -->
                    <div v-if="activeTab === 'trend'">
                        <div class="mb-3">
                            <label class="form-label small fw-bold">장소 선택</label>
                            <select class="form-select form-select-sm" v-model="selectedPlaceId"
                                @change="fetchPlaceTrend">
                                <option :value="null">전체 트렌드</option>
                                <option v-for="place in analysisData.hotspotRanking" :key="place.placeId"
                                    :value="place.placeId">
                                    {{ place.placeName }}
                                </option>
                            </select>
                        </div>

                        <!-- 트렌드 차트 -->
                        <div class="bg-light rounded p-3" style="height: 300px;">
                            <canvas ref="trendChart"></canvas>
                        </div>

                        <!-- 트렌드 요약 -->
                        <div class="mt-3" v-if="trendSummary">
                            <div class="row g-2">
                                <div class="col-4">
                                    <div class="border rounded p-2 text-center">
                                        <div class="small text-muted">최고 방문일</div>
                                        <div class="fw-bold small">{{ trendSummary.peakDate }}</div>
                                        <div class="text-primary">{{ trendSummary.peakCount }}회</div>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <div class="border rounded p-2 text-center">
                                        <div class="small text-muted">일평균</div>
                                        <div class="fw-bold text-success">{{ trendSummary.avgDaily }}</div>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <div class="border rounded p-2 text-center">
                                        <div class="small text-muted">추세</div>
                                        <div class="fw-bold" :class="trendSummary.trendClass">
                                            <i class="bi" :class="trendSummary.trendIcon"></i>
                                            {{ trendSummary.trendLabel }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 패턴 분석 탭 -->
                    <div v-if="activeTab === 'pattern'">
                        <!-- 요일별 분포 -->
                        <h6 class="fw-bold mb-2"><i class="bi bi-calendar3 me-1"></i>요일별 방문 분포</h6>
                        <div class="bg-light rounded p-3 mb-3" style="height: 200px;">
                            <canvas ref="dayOfWeekChart"></canvas>
                        </div>

                        <!-- 요일별 상세 -->
                        <div class="table-responsive mb-4">
                            <table class="table table-sm table-hover mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>요일</th>
                                        <th class="text-end">방문수</th>
                                        <th>인기 장소</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="day in analysisData.dayOfWeekDistribution" :key="day.dayOfWeek">
                                        <td>
                                            <span class="badge" :class="getDayBadgeClass(day.dayOfWeek)">
                                                {{ day.dayLabel }}
                                            </span>
                                        </td>
                                        <td class="text-end">{{ formatNumber(day.totalVisits) }}</td>
                                        <td class="small text-truncate" style="max-width: 150px;">
                                            {{ day.topPlaceName }}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- 월별 분포 -->
                        <h6 class="fw-bold mb-2"><i class="bi bi-calendar-month me-1"></i>월별 방문 추이</h6>
                        <div class="bg-light rounded p-3" style="height: 200px;">
                            <canvas ref="monthlyChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet.heat';
import Chart from 'chart.js/auto';
import axios from 'axios';

// Leaflet 마커 아이콘 설정
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

L.Icon.Default.mergeOptions({
    iconRetinaUrl: markerIcon2x,
    iconUrl: markerIcon,
    shadowUrl: markerShadow
});

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const apiClient = axios.create({ baseURL: API_BASE_URL });

export default {
    name: 'HotspotAnalysis',
    data() {
        return {
            // 날짜
            startDate: this.getDefaultStartDate(),
            endDate: this.getTodayDate(),

            // 상태
            loading: false,
            activeTab: 'ranking',
            selectedPlace: null,
            selectedPlaceId: null,

            // 데이터
            analysisData: {
                totalVisits: 0,
                totalUniqueUsers: 0,
                hotspotRanking: [],
                risingPlaces: [],
                dayOfWeekDistribution: [],
                monthlyDistribution: [],
                trendData: []
            },
            placeTrendData: [],

            // 지도
            map: null,
            heatLayer: null,
            markerLayerGroup: null,

            // 차트
            trendChart: null,
            dayOfWeekChart: null,
            monthlyChart: null,

            resizeObserver: null
        };
    },
    computed: {
        trendSummary() {
            const data = this.selectedPlaceId ? this.placeTrendData : this.analysisData.trendData;
            if (!data || data.length === 0) return null;

            const visits = data.map(d => d.visitCount);
            const maxVisit = Math.max(...visits);
            const maxIndex = visits.indexOf(maxVisit);
            const avgDaily = (visits.reduce((a, b) => a + b, 0) / visits.length).toFixed(1);

            // 간단한 추세 계산 (후반부 평균 vs 전반부 평균)
            const mid = Math.floor(data.length / 2);
            const firstHalf = visits.slice(0, mid).reduce((a, b) => a + b, 0) / mid || 0;
            const secondHalf = visits.slice(mid).reduce((a, b) => a + b, 0) / (data.length - mid) || 0;

            let trendLabel, trendClass, trendIcon;
            if (secondHalf > firstHalf * 1.1) {
                trendLabel = '상승';
                trendClass = 'text-success';
                trendIcon = 'bi-arrow-up';
            } else if (secondHalf < firstHalf * 0.9) {
                trendLabel = '하락';
                trendClass = 'text-danger';
                trendIcon = 'bi-arrow-down';
            } else {
                trendLabel = '유지';
                trendClass = 'text-secondary';
                trendIcon = 'bi-dash';
            }

            return {
                peakDate: data[maxIndex]?.dateLabel || '-',
                peakCount: maxVisit,
                avgDaily,
                trendLabel,
                trendClass,
                trendIcon
            };
        }
    },
    mounted() {
        this.$nextTick(() => {
            this.initMap();
            this.fetchAnalysis();
        });
    },
    watch: {
        activeTab(newTab) {
            this.$nextTick(() => {
                if (newTab === 'trend') {
                    this.renderTrendChart();
                } else if (newTab === 'pattern') {
                    this.renderDayOfWeekChart();
                    this.renderMonthlyChart();
                }
            });
        }
    },
    methods: {
        // === 유틸리티 ===
        getTodayDate() {
            return new Date().toISOString().split('T')[0];
        },
        getDefaultStartDate() {
            const d = new Date();
            d.setMonth(d.getMonth() - 1);
            return d.toISOString().split('T')[0];
        },
        formatNumber(num) {
            return num?.toLocaleString() || '0';
        },
        getRankBadgeClass(index) {
            if (index === 0) return 'bg-danger';
            if (index === 1) return 'bg-warning text-dark';
            if (index === 2) return 'bg-info';
            return 'bg-secondary';
        },
        getTrendBadgeClass(status) {
            switch (status) {
                case 'RISING': case 'NEW': return 'bg-success';
                case 'DECLINING': return 'bg-danger';
                default: return 'bg-secondary';
            }
        },
        getTrendIcon(status) {
            switch (status) {
                case 'RISING': case 'NEW': return 'bi-arrow-up';
                case 'DECLINING': return 'bi-arrow-down';
                default: return 'bi-dash';
            }
        },
        getDayBadgeClass(day) {
            if (day === 6) return 'bg-primary'; // 토요일
            if (day === 7) return 'bg-danger';  // 일요일
            return 'bg-secondary';
        },

        // === API 호출 ===
        async fetchAnalysis() {
            // 날짜 유효성 검사
            if (this.startDate > this.endDate) {
                alert('시작일이 종료일보다 늦을 수 없습니다.');
                return;
            }

            this.loading = true;
            try {
                const response = await apiClient.get('/api/admin/hotspot/analysis', {
                    params: {
                        startDate: this.startDate,
                        endDate: this.endDate,
                        type: 'DAILY',
                        limit: 10
                    }
                });
                this.analysisData = response.data;
                console.log('✅ 핫스팟 분석 로드 완료:', this.analysisData);

                this.updateHeatmap();

                if (this.activeTab === 'trend') {
                    this.renderTrendChart();
                } else if (this.activeTab === 'pattern') {
                    this.renderDayOfWeekChart();
                    this.renderMonthlyChart();
                }
            } catch (error) {
                console.error('❌ 핫스팟 분석 실패:', error);
                alert('데이터 로드에 실패했습니다.');
            } finally {
                this.loading = false;
            }
        },

        async fetchPlaceTrend() {
            if (!this.selectedPlaceId) {
                this.placeTrendData = [];
                this.renderTrendChart();
                return;
            }

            try {
                const response = await apiClient.get(`/api/admin/hotspot/trend/${this.selectedPlaceId}`, {
                    params: {
                        startDate: this.startDate,
                        endDate: this.endDate
                    }
                });
                this.placeTrendData = response.data;
                this.renderTrendChart();
            } catch (error) {
                console.error('❌ 장소 트렌드 로드 실패:', error);
            }
        },

        // === 지도 ===
        initMap() {
            this.map = L.map('hotspot-map').setView([36.5, 127.8], 7);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(this.map);

            this.markerLayerGroup = L.layerGroup().addTo(this.map);

            // ResizeObserver
            this.resizeObserver = new ResizeObserver(() => {
                if (this.map) this.map.invalidateSize();
            });
            const mapContainer = document.getElementById('hotspot-map');
            if (mapContainer) this.resizeObserver.observe(mapContainer);
        },

        updateHeatmap() {
            if (!this.map) return;

            // 기존 레이어 제거
            if (this.heatLayer) {
                this.map.removeLayer(this.heatLayer);
            }
            this.markerLayerGroup.clearLayers();

            const ranking = this.analysisData.hotspotRanking || [];
            if (ranking.length === 0) return;

            // 히트맵 데이터 생성
            const maxVisit = Math.max(...ranking.map(p => p.visitCount));
            const heatData = ranking.map(place => [
                place.latitude,
                place.longitude,
                place.visitCount / maxVisit // 정규화된 강도
            ]);

            // 히트맵 레이어 추가
            this.heatLayer = L.heatLayer(heatData, {
                radius: 35,
                blur: 25,
                maxZoom: 10,
                gradient: {
                    0.2: '#00BFFF',
                    0.4: '#00FF7F',
                    0.6: '#FFD700',
                    0.8: '#FF8C00',
                    1.0: '#FF0000'
                }
            }).addTo(this.map);

            // 마커 추가 (상위 10개)
            ranking.forEach((place, index) => {
                const size = 30 - index * 2;
                const icon = L.divIcon({
                    className: 'custom-hotspot-marker',
                    html: `<div style="
                        background: ${this.getHeatColor(place.visitCount, maxVisit)};
                        width: ${size}px;
                        height: ${size}px;
                        border-radius: 50%;
                        border: 2px solid white;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: white;
                        font-weight: bold;
                        font-size: 12px;
                    ">${index + 1}</div>`,
                    iconSize: [size, size],
                    iconAnchor: [size / 2, size / 2]
                });

                L.marker([place.latitude, place.longitude], { icon })
                    .bindPopup(`
                        <b>${index + 1}위: ${place.placeName}</b><br>
                        방문: ${place.visitCount}회<br>
                        방문자: ${place.uniqueUserCount}명
                    `)
                    .addTo(this.markerLayerGroup);
            });

            // 지도 범위 조정
            const bounds = ranking.map(p => [p.latitude, p.longitude]);
            if (bounds.length > 0) {
                this.map.fitBounds(bounds, { padding: [50, 50] });
            }
        },

        getHeatColor(value, max) {
            const ratio = value / max;
            if (ratio > 0.8) return '#FF0000';
            if (ratio > 0.6) return '#FF8C00';
            if (ratio > 0.4) return '#FFD700';
            if (ratio > 0.2) return '#00FF7F';
            return '#00BFFF';
        },

        selectPlace(place) {
            this.selectedPlace = place;
            if (this.map) {
                this.map.setView([place.latitude, place.longitude], 10);
            }
        },

        // === 차트 ===
        renderTrendChart() {
            const ctx = this.$refs.trendChart;
            if (!ctx) return;

            if (this.trendChart) {
                this.trendChart.destroy();
            }

            const data = this.selectedPlaceId ? this.placeTrendData : this.analysisData.trendData;
            if (!data || data.length === 0) return;

            this.trendChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.map(d => d.dateLabel),
                    datasets: [
                        {
                            label: '일별 방문',
                            data: data.map(d => d.visitCount),
                            borderColor: '#4A7CEC',
                            backgroundColor: 'rgba(74, 124, 236, 0.1)',
                            fill: true,
                            tension: 0.3
                        },
                        {
                            label: '7일 이동평균',
                            data: data.map(d => d.movingAverage),
                            borderColor: '#FF6B6B',
                            borderDash: [5, 5],
                            fill: false,
                            tension: 0.3,
                            pointRadius: 0
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'top' }
                    },
                    scales: {
                        x: {
                            ticks: {
                                maxTicksLimit: 10,
                                maxRotation: 45
                            }
                        },
                        y: { beginAtZero: true }
                    }
                }
            });
        },

        renderDayOfWeekChart() {
            const ctx = this.$refs.dayOfWeekChart;
            if (!ctx) return;

            if (this.dayOfWeekChart) {
                this.dayOfWeekChart.destroy();
            }

            const data = this.analysisData.dayOfWeekDistribution || [];
            if (data.length === 0) return;

            const colors = data.map(d => {
                if (d.dayOfWeek === 6) return '#4A7CEC'; // 토
                if (d.dayOfWeek === 7) return '#FF6B6B'; // 일
                return '#6C757D';
            });

            this.dayOfWeekChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.map(d => d.dayLabel),
                    datasets: [{
                        label: '방문수',
                        data: data.map(d => d.totalVisits),
                        backgroundColor: colors,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true } }
                }
            });
        },

        renderMonthlyChart() {
            const ctx = this.$refs.monthlyChart;
            if (!ctx) return;

            if (this.monthlyChart) {
                this.monthlyChart.destroy();
            }

            const data = this.analysisData.monthlyDistribution || [];
            if (data.length === 0) return;

            this.monthlyChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.map(d => d.monthLabel),
                    datasets: [{
                        label: '월별 방문',
                        data: data.map(d => d.totalVisits),
                        borderColor: '#28A745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        fill: true,
                        tension: 0.3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true } }
                }
            });
        }
    },

    beforeUnmount() {
        if (this.trendChart) this.trendChart.destroy();
        if (this.dayOfWeekChart) this.dayOfWeekChart.destroy();
        if (this.monthlyChart) this.monthlyChart.destroy();

        if (this.resizeObserver) {
            const mapContainer = document.getElementById('hotspot-map');
            if (mapContainer) this.resizeObserver.unobserve(mapContainer);
        }

        if (this.map) this.map.remove();
    }
};
</script>

<style scoped>
.nav-tabs .nav-link {
    color: #6c757d;
    border: none;
    padding: 0.5rem 1rem;
}

.nav-tabs .nav-link.active {
    color: #4A7CEC;
    border-bottom: 2px solid #4A7CEC;
    background: transparent;
}

.list-group-item.active {
    background-color: #4A7CEC;
    border-color: #4A7CEC;
}

#hotspot-map {
    min-height: 500px;
}
</style>

<!-- 전역 스타일 (scoped 없이) - Leaflet 마커용 -->
<style>
.custom-hotspot-marker {
    background: transparent !important;
    border: none !important;
}

/* 핵심 수정: Leaflet 마커 컨테이너 위치 고정 */
.leaflet-marker-icon.custom-hotspot-marker {
    margin: 0 !important;
}
</style>
