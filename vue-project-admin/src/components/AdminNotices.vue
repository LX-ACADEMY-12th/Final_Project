<template>
  <div>
    <ContentHeader iconClass="bi bi-megaphone" title="2. 공지사항 등록/관리" description="새로운 공지사항을 등록하고 기존 공지사항을 관리합니다." />
    <div class="d-flex justify-content-end mb-4">
      <button class="btn btn-primary" @click="openCreateModal">
        <i class="bi bi-plus-circle me-2"></i>
        공지사항 등록
      </button>
    </div>
    <div class="card shadow-sm border-0">
      <div class="card-header fs-5 fw-bold bg-white">
        공지사항 목록 ({{ notices.length }}개)
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th scope="col">ID</th>
                <th scope="col" style="min-width: 400px">제목</th>
                <th scope="col">작성자</th>
                <th scope="col">작성일</th>
                <th scope="col">조회수</th>
                <th scope="col">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="notice in notices" :key="notice.id">
                <td>{{ notice.id }}</td>
                <td>{{ notice.title }}</td>
                <!-- 🟢 [수정] notice.author → notice.authorName (user 테이블의 name) -->
                <!-- authorName이 없으면 author 표시, 둘 다 없으면 '-' 표시 -->
                <td>{{ notice.authorName || notice.author || '-' }}</td>
                <!-- 🟢 [수정] notice.date → formatDate(notice.createdAt) -->
                <!-- createdAt 날짜를 한국어 형식으로 포맷팅 -->
                <td>{{ formatDate(notice.createdAt) }}</td>
                <td>{{ notice.views }}</td>
                <td>
                  <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(notice)">
                    수정
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="$emit('delete-notice', notice.id)">
                    삭제
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 모달 컴포넌트 -->
    <NoticeModal :show="showNoticeModal" :notice="selectedNotice" :isEditMode="isEditMode" @close="closeModal"
      @save-notice="handleSaveNotice" />
  </div>
</template>

<script>
import ContentHeader from "./ContentHeader.vue";
import NoticeModal from "./NoticeModal.vue";

export default {
  name: "AdminNotices",
  components: { ContentHeader, NoticeModal },
  props: { notices: Array },
  emits: ["edit-notice", "delete-notice", "add-notice"],
  data() {
    return {
      showNoticeModal: false,
      selectedNotice: null,
      isEditMode: false,
    };
  },
  methods: {
    openCreateModal() {
      this.selectedNotice = null;
      this.isEditMode = false;
      this.showNoticeModal = true;
    },
    openEditModal(notice) {
      this.selectedNotice = { ...notice };
      this.isEditMode = true;
      this.showNoticeModal = true;
    },
    closeModal() {
      this.showNoticeModal = false;
      this.selectedNotice = null;
      this.isEditMode = false;
    },
    async handleSaveNotice(noticeData) {
      if (this.isEditMode) {
        this.$emit("edit-notice", noticeData); // 상위에 수정할 데이터만 전달
      } else {
        this.$emit("add-notice", noticeData);
      }
      this.closeModal();
    },
    formatDate(dateString) {
      if (!dateString) return "-";
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString("ko-KR", {
          year: "numeric", month: "2-digit", day: "2-digit",
        });
      } catch (e) { return "-"; }
    },
  },
};
</script>


<style scoped>
.card {
  border-radius: 8px;
}

.card-header {
  background-color: white !important;
  border-bottom: 1px solid #eef1f4;
  padding: 15px 20px;
}
</style>
