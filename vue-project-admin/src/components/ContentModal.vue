<template>
  <div class="modal fade" :class="{ show: show }" :style="{ display: show ? 'block' : 'none' }"
    @click.self="closeModal">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
      <div class="modal-content">

        <div class="modal-header">
          <h5 class="modal-title">{{ modalTitle }}</h5>
          <button type="button" class="btn-close" @click="closeModal"></button>
        </div>

        <div v-if="isLoading" class="modal-body text-center py-5">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>

        <div v-else class="modal-body">
          <form @submit.prevent="handleSaveClick">
            <div class="mb-3">
              <label class="form-label">이름 <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="form.name" required>
            </div>

            <div class="mb-3">
              <label class="form-label">주소</label>
              <div class="input-group">
                <input type="text" class="form-control" v-model="form.addressDetail"
                  :class="{ 'is-invalid': geocodingFeedback.type === 'danger' }">
                <button class="btn btn-outline-secondary" type="button" @click="geocodeAddress" :disabled="isGeocoding">
                  <span v-if="isGeocoding" class="spinner-border spinner-border-sm" role="status"></span>
                  좌표 찾기
                </button>
              </div>
              <div v-if="geocodingFeedback.message" class="mt-2"
                :class="geocodingFeedback.type === 'success' ? 'text-success' : 'text-danger'">
                <small><i class="bi"
                    :class="geocodingFeedback.type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'"></i>
                  {{ geocodingFeedback.message }}</small>
              </div>
            </div>

            <div class="mb-3">
              <div id="map" style="width:100%; height:250px; border-radius: 0.375rem;"></div>
              <small class="text-muted">지도를 클릭하거나 핀을 드래그하여 위치를 정확히 설정할 수 있습니다.</small>
            </div>

            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label">위도</label>
                <input type="number" step="any" class="form-control" v-model="form.latitude"
                  :class="{ 'is-valid': geocodingFeedback.type === 'success' }">
              </div>
              <div class="col-md-6">
                <label class="form-label">경도</label>
                <input type="number" step="any" class="form-control" v-model="form.longitude"
                  :class="{ 'is-valid': geocodingFeedback.type === 'success' }">
              </div>
            </div>

            <div class="row mb-3" v-if="modalType === 'halls' || modalType === 'exhibitions'">
              <label class="form-label">운영 기간</label>
              <div class="col-md-6">
                <input type="date" class="form-control" v-model="form.startDate">
              </div>
              <div class="col-md-6">
                <input type="date" class="form-control" v-model="form.endDate">
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label">운영 시간</label>
              <input type="text" class="form-control" v-model="form.openingHours"
                placeholder="예: 09:00 - 18:00 (월요일 휴무)">
            </div>

            <div class="mb-3">
              <label class="form-label">입장료</label>
              <input type="text" class="form-control" v-model="form.admissionFee" placeholder="예: 성인 5,000원">
            </div>

            <div class="mb-3">
              <label class="form-label">메인 이미지</label>
              <input type="file" class="form-control" @change="handleFileChange" accept="image/*">
              <div class="mt-2">
                <img v-if="previewUrl" :src="previewUrl" class="img-thumbnail" style="max-height: 200px;" alt="미리보기" />
                <small v-if="isEditMode && !previewUrl && form.mainImageUrl" class="text-muted">
                  <br>기존 이미지: {{ form.mainImageUrl }} (새 파일 선택 시 교체됨)
                </small>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label">상세 설명</label>
              <textarea class="form-control" rows="5" v-model="form.description"></textarea>
            </div>

            <hr>

            <template v-if="modalType === 'exhibitions'">
              <div class="mb-3">
                <label class="form-label">소속 전시관 <span class="text-danger">*</span></label>
                <select class="form-select" v-model="form.hallId" required>
                  <option :value="null">-- 전시관 선택 --</option>
                  <option v-for="hall in allHalls" :key="hall.hallId" :value="hall.hallId">
                    {{ hall.hallName }}
                  </option>
                </select>
              </div>
            </template>

            <template v-if="modalType === 'exhibitions' || modalType === 'places'">
              <div class="mb-3">
                <label class="form-label">학년</label>
                <div>
                  <span v-for="grade in allGrades" :key="grade.gradeId" class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox" :value="grade.gradeId" v-model="form.gradeIds">
                    <label class="form-check-label">{{ grade.gradeName }}</label>
                  </span>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">세부 단원 (검색 및 다중 선택)</label>
                <Multiselect v-model="form.subCategoryIds" :options="allSubCategories" mode="tags"
                  placeholder="검색하여 세부 단원 추가" :searchable="true" :close-on-select="false" value-prop="subCategoryId"
                  track-by="subCategoryId" label="displayName" class="form-multiselect" :classes="{
                    tag: 'badge bg-primary me-1',
                    tagRemove: 'multiselect-tag-remove',
                  }" />
              </div>
            </template>

          </form>
        </div>

        <div class="modal-footer" v-if="!isLoading">
          <button type="button" class="btn btn-secondary" @click="closeModal">닫기</button>
          <button type="button" class="btn btn-primary" @click="handleSaveClick" :disabled="isSaving">
            <span v-if="isSaving" class="spinner-border spinner-border-sm" role="status"></span>
            저장
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

// 각 타입별 DTO 기본값
const defaultForm = {
  halls: {
    hallName: '', scienceCenterName: '', addressDetail: '', latitude: null, longitude: null,
    startDate: null, endDate: null, admissionFee: '', openingHours: '', description: '', mainImageUrl: '',
  },
  exhibitions: {
    exhibitionName: '', hallId: null, addressDetail: '', latitude: null, longitude: null, type: 'PERMANENT',
    startDate: null, endDate: null, admissionFee: '', openingHours: '', description: '', mainImageUrl: '',
    gradeIds: [], subCategoryIds: [],
  },
  places: {
    placeName: '', addressDetail: '', latitude: null, longitude: null, placeType: 'PLACE',
    admissionFee: '', openingHours: '', description: '', mainImageUrl: '',
    gradeIds: [], subCategoryIds: [],
  }
}

export default {
  components: {
    Multiselect,
  },
  props: {
    show: { type: Boolean, default: false },
    modalType: { type: String, required: true },
    itemIdToEdit: { type: Number, default: null }
  },
  emits: ['close', 'save'],
  data() {
    return {
      isLoading: false,
      isSaving: false,
      isGeocoding: false,
      form: {},
      selectedFile: null,
      previewUrl: null,

      allHalls: [],
      allGrades: [],
      allSubCategories: [],

      geocodingFeedback: { message: '', type: '' },

      // 카카오맵 인스턴스 (Vue의 반응형으로 관리하지 않음)
      mapInstance: null,
      markerInstance: null,
    };
  },
  computed: {
    isEditMode() {
      return this.itemIdToEdit != null;
    },
    modalTitle() {
      const typeName = { halls: '전시관', exhibitions: '전시', places: '체험 장소' }[this.modalType];
      return this.isEditMode ? `${typeName} 수정 (ID: ${this.itemIdToEdit})` : `${typeName} 생성`;
    }
  },
  methods: {
    closeModal() {
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl);
      }
      this.$emit('close');
    },

    async loadModalData() {
      this.isLoading = true;
      try {
        // Promise.all로 공통 데이터를 병렬로 API 호출
        const [hallsRes, gradesRes, subCategoriesRes] = await Promise.all([
          axios.get('/api/admin/content/common/halls'),
          axios.get('/api/admin/content/common/grades'),
          axios.get('/api/admin/content/common/subcategories')
        ]);

        this.allHalls = hallsRes.data;
        this.allGrades = gradesRes.data;
        this.allSubCategories = subCategoriesRes.data.map(sub => ({
          ...sub,
          displayName: `[${sub.gradeName} / ${sub.mainCategoryName}] ${sub.subCategoryName}`
        }));

        // [U] 수정 모드
        if (this.isEditMode) {
          const response = await axios.get(`/api/admin/content/${this.modalType}/${this.itemIdToEdit}`);
          this.form = response.data;

          if (this.modalType === 'halls') this.form.name = this.form.hallName;
          else if (this.modalType === 'exhibitions') this.form.name = this.form.exhibitionName;
          else if (this.modalType === 'places') this.form.name = this.form.placeName;

          this.form.gradeIds = this.form.gradeIds || [];
          this.form.subCategoryIds = this.form.subCategoryIds || [];
        }
        // [C] 생성 모드
        else {
          this.form = { ...defaultForm[this.modalType] };
          this.form.name = '';
        }

        this.selectedFile = null;
        this.previewUrl = null;

      } catch (error) {
        console.error("모달 데이터 로드 실패:", error);
        alert("데이터를 불러오는 데 실패했습니다.");
        this.closeModal();
      } finally {
        this.isLoading = false;
        // 폼 데이터 로드가 끝나고, 템플릿이 렌더링 된 후 지도를 초기화
        this.$nextTick(() => {
          this.initMap();
        });
      }
    },

    // 1. 지도 초기화
    initMap() {
      if (!window.kakao || !document.getElementById('map')) {
        console.error("카카오맵 SDK가 로드되지 않았거나, map div를 찾을 수 없습니다.");
        return;
      }

      const mapContainer = document.getElementById('map');
      let lat = 37.5665; // 기본 위도 (서울시청)
      let lng = 126.9780; // 기본 경도

      // 수정 모드이고 폼에 좌표가 있으면, 그 좌표를 기본값으로 사용
      if (this.form.latitude && this.form.longitude) {
        lat = this.form.latitude;
        lng = this.form.longitude;
      }

      const mapOption = {
        center: new window.kakao.maps.LatLng(lat, lng),
        level: 5
      };

      this.mapInstance = new window.kakao.maps.Map(mapContainer, mapOption);
      this.markerInstance = new window.kakao.maps.Marker({
        position: new window.kakao.maps.LatLng(lat, lng),
        draggable: true
      });
      this.markerInstance.setMap(this.mapInstance);

      // 지도 클릭 이벤트
      window.kakao.maps.event.addListener(this.mapInstance, 'click', (mouseEvent) => {
        this.updateFormFromMapEvent(mouseEvent.latLng);
      });

      // 마커 드래그 종료 이벤트
      window.kakao.maps.event.addListener(this.markerInstance, 'dragend', () => {
        this.updateFormFromMapEvent(this.markerInstance.getPosition());
      });
    },

    // 2. 지도 이벤트(클릭/드래그) -> 폼 업데이트
    async updateFormFromMapEvent(latLng) {
      const newLat = latLng.getLat();
      const newLng = latLng.getLng();

      this.markerInstance.setPosition(latLng);
      this.form.latitude = newLat.toFixed(7);
      this.form.longitude = newLng.toFixed(7);

      this.geocodingFeedback = { message: '좌표를 갱신했습니다.', type: 'success' };

      // 좌표 -> 주소 역방향 변환 API 호출
      try {
        const response = await axios.post('/api/admin/content/reverse-geocode', {
          latitude: newLat,
          longitude: newLng
        });
        if (response.data && response.data.address) {
          this.form.addressDetail = response.data.address;
          this.geocodingFeedback.message = '좌표와 주소를 갱신했습니다.';
        }
      } catch (error) {
        console.error("역방향 지오코딩 실패:", error);
        this.geocodingFeedback.message = '좌표는 갱신했으나, 주소를 찾는 데는 실패했습니다.';
        this.geocodingFeedback.type = 'danger';
      }
    },

    // 3. 폼 -> 지도로 갱신 (헬퍼)
    setMapCenterAndMarker(lat, lng) {
      if (!this.mapInstance) return;

      const newPos = new window.kakao.maps.LatLng(lat, lng);
      this.mapInstance.panTo(newPos);
      this.markerInstance.setPosition(newPos);
    },

    // 4. '좌표 찾기' 버튼 클릭
    async geocodeAddress() {
      if (!this.form.addressDetail) {
        this.geocodingFeedback = { message: '주소를 입력해주세요.', type: 'danger' };
        return;
      }
      this.isGeocoding = true;
      this.geocodingFeedback = { message: '', type: '' };

      try {
        const response = await axios.post('/api/admin/content/geocode', {
          address: this.form.addressDetail
        });

        if (response.data && response.data.latitude) {
          const lat = response.data.latitude;
          const lng = response.data.longitude;

          this.form.latitude = lat;
          this.form.longitude = lng;
          this.geocodingFeedback = { message: '좌표를 성공적으로 반영했습니다.', type: 'success' };

          // 지도에도 반영
          this.setMapCenterAndMarker(lat, lng);

        } else {
          this.form.latitude = null;
          this.form.longitude = null;
          this.geocodingFeedback = { message: '유효한 좌표를 찾을 수 없습니다. 주소를 확인해주세요.', type: 'danger' };
        }
      } catch (error) {
        console.error('지오코딩 실패:', error);
        this.form.latitude = null;
        this.form.longitude = null;
        this.geocodingFeedback = { message: '좌표 검색 중 오류가 발생했습니다.', type: 'danger' };
      } finally {
        this.isGeocoding = false;
      }
    },

    // 5. 파일 선택
    handleFileChange(event) {
      const file = event.target.files[0];
      if (!file) return;
      this.selectedFile = file;
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl);
      }
      this.previewUrl = URL.createObjectURL(file);
    },

    // 6. 저장
    async handleSaveClick() {
      this.isSaving = true;
      const formData = new FormData();

      if (this.selectedFile) {
        formData.append('mainImage', this.selectedFile);
      }

      const dto = { ...this.form };
      if (this.modalType === 'halls') dto.hallName = dto.name;
      else if (this.modalType === 'exhibitions') dto.exhibitionName = dto.name;
      else if (this.modalType === 'places') dto.placeName = dto.name;
      delete dto.name;

      if (dto.startDate === '') dto.startDate = null;
      if (dto.endDate === '') dto.endDate = null;

      formData.append('dto', JSON.stringify(dto));

      try {
        if (this.isEditMode) {
          await axios.put(`/api/admin/content/${this.modalType}/${this.itemIdToEdit}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
        } else {
          await axios.post(`/api/admin/content/${this.modalType}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
        }
        this.$emit('save');
      } catch (error) {
        console.error("저장 실패:", error);
        alert("저장 중 오류가 발생했습니다.");
      } finally {
        this.isSaving = false;
      }
    }
  },
  created() {
    this.loadModalData();
  },
  watch: {
    'form.addressDetail'(newVal) {
      if (this.geocodingFeedback.type === 'danger') {
        this.geocodingFeedback = { message: '', type: '' };
      }
    },
    'form.latitude'(newVal) {
      if (this.mapInstance && newVal && this.form.longitude) {
        this.setMapCenterAndMarker(newVal, this.form.longitude);
      }
    },
    'form.longitude'(newVal) {
      if (this.mapInstance && newVal && this.form.latitude) {
        this.setMapCenterAndMarker(this.form.latitude, newVal);
      }
    }
  }
};
</script>

<style scoped>
.modal.show {
  background-color: rgba(0, 0, 0, 0.5);
}

.form-multiselect {
  --ms-border-color: #dee2e6;
  --ms-border-radius: 0.375rem;
  --ms-min-height: calc(1.5em + 0.75rem + 2px);
  --ms-line-height: 1.5;
  --ms-font-size: 1rem;
  --ms-placeholder-color: #6c757d;
  --ms-tag-bg: #0d6efd;
  --ms-tag-font-weight: 600;
  --ms-tag-radius: 0.375rem;
}

:global(.multiselect-tag-remove) {
  width: 1rem;
  height: 1rem;
  line-height: 1rem;
  margin-left: 0.25rem;
}

:global(.multiselect-tag-remove-icon) {
  width: 0.5rem;
  height: 0.5rem;
}

/* Bootstrap 유효성 검사 스타일 (선택적) */
.form-control.is-valid {
  border-color: var(--bs-success);
}

.form-control.is-invalid {
  border-color: var(--bs-danger);
}
</style>