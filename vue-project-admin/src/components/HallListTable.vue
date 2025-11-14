<template>
    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover table-striped mb-0 align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>전시관명</th>
                            <th>주소</th>
                            <th>전시 개수</th>
                            <th>관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-if="halls.length === 0">
                            <td colspan="5" class="text-center py-5 text-muted">데이터가 없습니다.</td>
                        </tr>
                        <tr v-for="hall in halls" :key="hall.id">
                            <td>{{ hall.id }}</td>
                            <td>
                                <!-- ✅ 이미지 로드 실패 시 빈 박스 표시 -->
                                <img v-if="!failedImages.has(hall.imageUrl)" :src="getFullImageUrl(hall.imageUrl)"
                                    @error="handleImageError(hall.imageUrl, $event)" class="me-2 rounded"
                                    style="width: 40px; height: 40px; object-fit: cover" alt="img" />
                                <!-- ✅ 실패한 경우 회색 박스 표시 -->
                                <span v-else class="me-2 rounded d-inline-block bg-secondary align-middle"
                                    style="width: 40px; height: 40px;"></span>
                                {{ hall.name }}
                            </td>
                            <td>{{ hall.address }}</td>
                            <td>
                                <button class="btn btn-link btn-sm p-0" @click="$emit('view-exhibitions', hall)">
                                    {{ hall.exhibitionCount }}개
                                </button>
                            </td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary me-2"
                                    @click="$emit('edit', hall.id)">수정</button>
                                <button class="btn btn-sm btn-outline-danger"
                                    @click="$emit('delete', hall.id)">삭제</button>
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
    name: "HallListTable",
    props: { halls: { type: Array, default: () => [] } },
    emits: ['edit', 'delete', 'view-exhibitions'],

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
                return null;
            }
            if (imageUrl.startsWith('/')) {
                return this.backendUrl + imageUrl;
            }
            return imageUrl;
        },

        // ✅ 이미지 로드 실패 처리
        handleImageError(imageUrl, event) {
            console.log('Image load failed:', imageUrl);
            // 실패한 URL 기록
            this.failedImages.add(imageUrl);
            // 이벤트 핸들러 제거 (무한 루프 방지)
            event.target.onerror = null;
        }
    }
}
</script>