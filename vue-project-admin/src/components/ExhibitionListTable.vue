<template>
    <div class="card shadow-sm border-0">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover table-striped mb-0 align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>전시명</th>
                            <th>타입</th>
                            <th>과학영역</th>
                            <th>학년</th>
                            <th>소속 전시관</th>
                            <th>관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-if="exhibitions.length === 0">
                            <td colspan="7" class="text-center py-5 text-muted">데이터가 없습니다.</td>
                        </tr>
                        <tr v-for="exh in exhibitions" :key="exh.id">
                            <td>{{ exh.id }}</td>
                            <td>
                                <!-- ✅ 이미지 로드 실패 시 빈 박스 표시 -->
                                <img v-if="!failedImages.has(exh.imageUrl)" :src="getFullImageUrl(exh.imageUrl)"
                                    @error="handleImageError(exh.imageUrl, $event)" class="me-2 rounded"
                                    style="width: 40px; height: 40px; object-fit: cover" alt="이미지" />
                                <!-- ✅ 실패한 경우 회색 박스 표시 -->
                                <span v-else class="me-2 rounded d-inline-block bg-secondary align-middle"
                                    style="width: 40px; height: 40px;"></span>
                                {{ exh.title }}
                            </td>
                            <td>{{ displayExhibitionType(exh.type) }}</td>
                            <td>{{ exh.subject }}</td>
                            <td>{{ exh.grade }}</td>
                            <td>{{ exh.place }}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary me-2"
                                    @click="$emit('edit', exh.id)">수정</button>
                                <button class="btn btn-sm btn-outline-danger"
                                    @click="$emit('delete', exh.id)">삭제</button>
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
    name: "ExhibitionListTable",
    props: { exhibitions: { type: Array, default: () => [] } },
    emits: ['edit', 'delete'],

    data() {
        return {
            backendUrl: 'http://localhost:8080',
            // ✅ 실패한 이미지 URL 추적
            failedImages: new Set()
        };
    },

    methods: {
        // 전시 타입을 한글로 변환하는 메서드
        displayExhibitionType(type) {
            switch (type) {
                case 'PERMANENT':
                    return '상설';
                case 'SPECIAL':
                    return '특별';
                case 'TEMPORARY':
                    return '임시';
                default:
                    return type;
            }
        },
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