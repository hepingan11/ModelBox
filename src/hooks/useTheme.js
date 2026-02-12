import { computed } from 'vue'
import { store } from '@/store/index.js'

export const useTheme = () => {
    const themeClass = computed(() => store.state.theme)

    const toggleTheme = () => {
        store.toggleTheme()
    }

    return {
        themeClass,
        toggleTheme
    }
}
