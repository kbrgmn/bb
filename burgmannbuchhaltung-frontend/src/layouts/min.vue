<template class="h-full">
    <div class="h-full">
        <slot/>
    </div>
</template>

<script setup>
const route = useRoute()
useHead({
    titleTemplate: (titleChunk) => {
        return titleChunk ? `${titleChunk} - BurgmannBuchhaltung` :
            (route.meta.title ? `${route.meta.title} - BurgmannBuchhaltung` : 'BurgmannBuchhaltung');
    },
    meta: [{
        property: 'og:title',
        content: `BurgmannBuchhaltung - ${route.meta.title}`
    }]
})

/*
useHead({
  titleTemplate: (titleChunk) => {
    return titleChunk ? `${titleChunk} - BurgmannBuchhaltung` : 'BurgmannBuchhaltung';
  }
})
 */
</script>
