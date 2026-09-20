/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        hero: {
          gold: '#F59E0B',
          goldDark: '#D97706',
          goldLight: '#FDE68A',
          navy: '#0A1128',
          navySurface: '#101F42',
          navyCard: '#1E293B',
          emerald: '#059669',
          crimson: '#DC2626'
        }
      },
      fontFamily: {
        sans: ['Tajawal', 'Cairo', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
