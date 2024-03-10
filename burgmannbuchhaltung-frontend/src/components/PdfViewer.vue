<template>
  <ClientOnly>
    <div ref="el">
      <vue-pdf-embed ref="pdf" :page="1" class="shadow" :source="`/r/org/${orgId}/receipt-import/${sessionId}/${fileId}/pdf`" @rendered="handleDocumentRender"/>
        <!-- can be replaced with:
        <object type="application/pdf"
          data="/media/examples/In-CC0.pdf"
          width="250"
          height="200">
        </object>
        -->
    </div>
  </ClientOnly>
</template>

<script setup>

import {useResizeObserver} from '@vueuse/core'
import VuePdfEmbed from 'vue-pdf-embed'
import {useOrganization} from "~/composables/organizations";

const route = useRoute()
const orgId = useOrganization()

const sessionId = route.params.sessionId
const fileId = route.params.fileId

let noObserverRegistered = true
let initialRender = true

const el = ref(null)
const pdf = ref(null)

const renderPdf = useDebounceFn(() => {
    console.log("Rendering PDF...")
    pdf.value.render()
}, 1000)

function handleDocumentRender() {
    console.log("PDF Document rendered.")

    if (noObserverRegistered) {
        console.log("Registered PDF resize observer.")
        useResizeObserver(el, () => {
            console.log("Detected PDF resize.")
            if (!initialRender) {
                renderPdf()
            } else {
                initialRender = false
            }
        })

        noObserverRegistered = false
    }
}

// const pdfWidth =

</script>

<style scoped>

</style>
