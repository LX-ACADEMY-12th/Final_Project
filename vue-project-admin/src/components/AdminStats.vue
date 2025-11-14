<template>
  <div>
    <ContentHeader
      iconClass="bi bi-bar-chart-line"
      title="4. 스탬프 통계 데이터 보기"
      description="서비스 이용 현황 및 스탬프 데이터를 통계로 분석합니다."
    />
    <div class="row g-4">
      <div class="col-md-6 col-xl-3">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="fs-1 text-primary me-3">
                <i class="bi bi-calendar-check"></i>
              </div>
              <div>
                <div class="fs-5 text-muted">오늘 획득 스탬프</div>
                <div class="fs-2 fw-bold">{{ stats.todayStamps }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6 col-xl-3">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="fs-1 text-success me-3">
                <i class="bi bi-bar-chart-steps"></i>
              </div>
              <div>
                <div class="fs-5 text-muted">총 누적 스탬프</div>
                <div class="fs-2 fw-bold">{{ stats.totalStamps }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6 col-xl-3">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="fs-1 text-danger me-3">
                <i class="bi bi-star-fill"></i>
              </div>
              <div>
                <div class="fs-5 text-muted">인기 장소 (최다 스탬프)</div>
                <div class="fs-4 fw-bold">{{ stats.topPlace }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6 col-xl-3">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="fs-1 text-warning me-3">
                <i class="bi bi-people-fill"></i>
              </div>
              <div>
                <div class="fs-5 text-muted">활성 사용자 (오늘)</div>
                <div class="fs-2 fw-bold">{{ stats.activeUsers }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row mt-4">
      <div class="col-12">
        <div class="card shadow-sm border-0">
          <div class="card-header fs-5 fw-bold bg-white">
            일별 스탬프 획득 추이
          </div>
          <div
            class="card-body d-flex align-items-center justify-content-center"
            style="min-height: 300px"
          >
            <p class="text-muted">
              (이 영역에 Chart.js 또는 ApexCharts 등을 사용하여 그래프를 그립니다.)
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ContentHeader from "./ContentHeader.vue";

export default {
  name: "AdminStats",
  components: { ContentHeader },
  props: {
    stats: Object, // 통계 데이터를 받습니다.
  },
};
</script>

<style scoped>
/* 통계 카드 관련 스타일과 일반 카드 스타일을 이곳에 모읍니다. */
.card {
  border-radius: 8px;
}
.card-header {
  background-color: white !important;
  border-bottom: 1px solid #eef1f4;
  padding: 15px 20px;
}
.card .fs-1 {
  color: var(--bs-primary); /* Bootstrap primary 색상 사용 */
}
.card .fs-5 {
  color: #667085 !important;
}
</style>