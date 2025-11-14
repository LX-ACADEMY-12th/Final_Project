<template>
    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover table-striped mb-0 align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>장소명</th>
                            <th>타입</th>
                            <th>과학영역</th>
                            <th>학년</th>
                            <th>주소</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr v-if="places.length === 0">
                            <td colspan="7" class="text-center py-5 text-muted">데이터가 없습니다.</td>
                        </tr>
                        <tr v-for="place in places" :key="place.id">
                            <td>{{ place.id }}</td>
                            <td>
                                <!-- ✅ 이미지 로드 실패 시 빈 박스 표시 -->
                                <img v-if="!failedImages.has(place.imageUrl)" :src="getFullImageUrl(place.imageUrl)"
                                    @error="handleImageError(place.imageUrl, $event)" class="me-2 rounded"
                                    style="width: 40px; height: 40px; object-fit: cover" alt="이미지" />
                                <!-- ✅ 실패한 경우 회색 박스 표시 -->
                                <span v-else class="me-2 rounded d-inline-block bg-secondary align-middle"
                                    style="width: 40px; height: 40px;"></span>
                                {{ place.title }}
                            </td>
                            <td>
                                <span class="badge"
                                    :class="place.type === 'PERMANENT' ? 'bg-success-subtle text-success-emphasis' : 'bg-info-subtle text-info-emphasis'">
                                    {{ place.type === "PLACE" ? "답사" : place.type }}
                                </span>
                            </td>
                            <td>{{ place.subject }}</td>
                            <td>{{ place.grade }}</td>
                            <td>{{ place.place }}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary me-2"
                                    @click="$emit('edit', place.id)">수정</button>
                                <button class="btn btn-sm btn-outline-danger"
                                    @click="$emit('delete', place.id)">삭제</button>
                            </td>
                        </tr>
                    </tbody>

                </table>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "PlaceListTable",
    props: {
        places: {
            type: Array,
            default: () => [],
        },
    },
    emits: ["edit", "delete"],

    data() {
        return {
            backendUrl: 'http://localhost:8080',
            // ✅ 실패한 이미지 URL 추적
            failedImages: new Set()
        };
    },

    methods: {
        getFullImageUrl(imageUrl) {
            if (!imageUrl) {
                return null; // ✅ null 반환 (v-if에서 처리)
            }
            if (imageUrl.startsWith('/')) {
                return this.backendUrl + imageUrl;
            }
            return imageUrl;
        },

        /**
         * ✅ 이미지 로드 실패 처리 (완전한 무한 루프 방지)
         */
        handleImageError(imageUrl, event) {
            console.log('Image load failed:', imageUrl);

            // 1. 실패한 URL을 Set에 추가
            this.failedImages.add(imageUrl);

            // 2. 이벤트 핸들러 제거 (추가 안전장치)
            event.target.onerror = null;
        }
    }
};
</script>