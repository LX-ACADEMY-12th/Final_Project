<template>
  <main class="main-content">
    <AdminReviews v-if="currentView === 'reviews'" :reviews="reviews" :totalElements="reviewsTotalElements"
      :currentPage="reviewsCurrentPage" :totalPages="reviewsTotalPages" @delete-review="$emit('delete-review', $event)"
      @page-change="$emit('review-page-change', $event)" @search="$emit('search-reviews', $event)" />

    <AdminNotices v-else-if="currentView === 'notices'" :notices="notices" @edit-notice="$emit('edit-notice', $event)"
      @delete-notice="$emit('delete-notice', $event)" @add-notice="$emit('add-notice', $event)" />

    <AdminContent v-else-if="currentView === 'places'" :places="places" :totalElements="totalElements"
      :currentPage="currentPage" :totalPages="totalPages" @page-change="$emit('page-change', $event)"
      @edit-place="(...args) => $emit('edit-place', ...args)" @delete-place="$emit('delete-place', $event)"
      @add-place="(...args) => $emit('add-place', ...args)" />

    <AdminStats v-else-if="currentView === 'stats'" :stats="stats" />
  </main>
</template>

<script>
import AdminReviews from "./AdminReviews.vue";
import AdminNotices from "./AdminNotices.vue";
import AdminPlaces from "./AdminContent.vue";
import AdminStats from "./AdminStats.vue";
import AdminContent from "./AdminContent.vue";

export default {
  name: "ContentView",
  components: {
    AdminReviews,
    AdminNotices,
    AdminPlaces,
    AdminStats,
    AdminContent
  },
  props: {
    currentView: {
      type: String,
      required: true,
    },
    stats: Object,
    reviews: Array,
    notices: Array,
    places: Array,
    reviewsTotalElements: {
      type: Number,
      default: 0,
    },
    reviewsCurrentPage: {
      type: Number,
      default: 1,
    },
    reviewsTotalPages: {
      type: Number,
      default: 0,
    },
    totalElements: {
      type: Number,
      default: 0,
    },
    currentPage: {
      type: Number,
      default: 1,
    },
    totalPages: {
      type: Number,
      default: 0,
    },
  },
  emits: [
    "delete-review",
    "review-page-change",
    "search-reviews",
    "edit-place",
    "delete-place",
    "edit-notice",
    "delete-notice",
    "add-notice",
    "add-place",
    "page-change",
  ],
};
</script>

<style scoped>
.main-content {
  flex-grow: 1;
  overflow-y: auto;
  background-color: #EEF1F4;
  padding: 30px !important;
}
</style>
