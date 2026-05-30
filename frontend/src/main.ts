import './style.css'

// Empty in dev (Vite proxies /api → backend). Set VITE_API_BASE for preview/production.
const API_BASE = import.meta.env.VITE_API_BASE ?? ''
const URL_STORAGE_KEY = 'load-simulator:last-url'

interface LoadTestRequest {
  url: string
  method: string
  virtualUsers: number
  durationSeconds: number
}

interface LoadTestResult {
  totalRequests: number
  successfulRequests: number
  failedRequests: number
  averageResponseTimeMs: number
  minResponseTimeMs: number
  maxResponseTimeMs: number
  p95ResponseTimeMs: number
  requestsPerSecond: number
  errorRate: number
}

interface ApiErrorResponse {
  message: string
  errors?: Record<string, string>
}

type FieldName = 'url' | 'virtualUsers' | 'durationSeconds'

// ── DOM refs ──────────────────────────────────────────────────────────────────

const layout        = document.getElementById('layout')                as HTMLDivElement
const form          = document.getElementById('load-test-form')       as HTMLFormElement
const configCard    = document.getElementById('config-card')          as HTMLElement
const runBtn        = document.getElementById('run-btn')               as HTMLButtonElement
const runAgainBtn   = document.getElementById('run-again-btn')         as HTMLButtonElement
const urlInput      = document.getElementById('url')                   as HTMLInputElement
const usersInput    = document.getElementById('virtualUsers')          as HTMLInputElement
const durationInput = document.getElementById('durationSeconds')       as HTMLInputElement

const urlError      = document.getElementById('url-error')             as HTMLSpanElement
const usersError    = document.getElementById('virtualUsers-error')    as HTMLSpanElement
const durationError = document.getElementById('durationSeconds-error') as HTMLSpanElement

const helpBtn           = document.getElementById('help-btn')              as HTMLButtonElement
const helpModalBackdrop = document.getElementById('help-modal-backdrop')   as HTMLDivElement
const helpModal         = document.getElementById('help-modal')            as HTMLDivElement
const helpModalClose    = document.getElementById('help-modal-close')      as HTMLButtonElement

const statusArea    = document.getElementById('status-area')           as HTMLDivElement
const loadingPrimary   = document.getElementById('loading-primary')    as HTMLParagraphElement
const loadingSecondary = document.getElementById('loading-secondary')  as HTMLParagraphElement
const loadingCountdown = document.getElementById('loading-countdown')  as HTMLParagraphElement

const errorArea     = document.getElementById('error-area')            as HTMLDivElement
const errorTitle    = document.getElementById('error-title')           as HTMLParagraphElement
const errorDetail   = document.getElementById('error-detail')          as HTMLParagraphElement

const resultsCard   = document.getElementById('results-card')          as HTMLElement
const resultsMeta   = document.getElementById('results-meta')          as HTMLDivElement

const mTotal      = document.getElementById('m-total')       as HTMLSpanElement
const mSuccess    = document.getElementById('m-success')     as HTMLSpanElement
const mFailed     = document.getElementById('m-failed')      as HTMLSpanElement
const mFailedCard = document.getElementById('m-failed-card') as HTMLDivElement
const mAvg        = document.getElementById('m-avg')         as HTMLSpanElement
const mMin        = document.getElementById('m-min')         as HTMLSpanElement
const mMax        = document.getElementById('m-max')         as HTMLSpanElement
const mP95        = document.getElementById('m-p95')         as HTMLSpanElement
const mRps        = document.getElementById('m-rps')         as HTMLSpanElement
const mErrorRate      = document.getElementById('m-error-rate')       as HTMLSpanElement
const mErrorRateCard  = document.getElementById('m-error-rate-card')  as HTMLDivElement

const fieldErrors: Record<FieldName, HTMLSpanElement> = {
  url: urlError,
  virtualUsers: usersError,
  durationSeconds: durationError,
}

const fieldInputs: Record<FieldName, HTMLInputElement> = {
  url: urlInput,
  virtualUsers: usersInput,
  durationSeconds: durationInput,
}

let countdownTimerId: number | null = null
let helpTriggerElement: HTMLElement | null = null

// ── Helpers ───────────────────────────────────────────────────────────────────

function fmt(n: number, decimals = 2): string {
  return n.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })
}

function fmtInt(n: number): string {
  return n.toLocaleString('en-US')
}

function setRunning(running: boolean): void {
  urlInput.disabled      = running
  usersInput.disabled    = running
  durationInput.disabled = running
  runBtn.disabled        = running
  runAgainBtn.disabled   = running
}

function showStatus(): void {
  statusArea.classList.remove('hidden')
  errorArea.classList.add('hidden')
  resultsCard.classList.add('hidden')
  configCard.classList.remove('card--muted')
}

function hideStatus(): void {
  statusArea.classList.add('hidden')
}

function scrollIntoView(target: Element): void {
  target.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function stopCountdown(): void {
  if (countdownTimerId !== null) {
    window.clearInterval(countdownTimerId)
    countdownTimerId = null
  }
  loadingCountdown.textContent = ''
}

function startCountdown(durationSeconds: number): void {
  stopCountdown()

  const startedAt = Date.now()
  const totalMs = durationSeconds * 1000

  const render = (): void => {
    const elapsedMs = Date.now() - startedAt
    const remainingMs = Math.max(0, totalMs - elapsedMs)
    const remainingSeconds = (remainingMs / 1000).toFixed(1)

    loadingCountdown.textContent =
      remainingMs > 0
        ? `Estimated time remaining: ${remainingSeconds}s`
        : 'Wrapping up results...'
  }

  render()
  countdownTimerId = window.setInterval(render, 100)
}

function clearFieldErrors(): void {
  for (const field of Object.keys(fieldErrors) as FieldName[]) {
    fieldErrors[field].textContent = ''
    fieldErrors[field].classList.add('hidden')
    fieldInputs[field].classList.remove('input--error')
  }
}

function setFieldError(field: FieldName, message: string): void {
  fieldErrors[field].textContent = message
  fieldErrors[field].classList.remove('hidden')
  fieldInputs[field].classList.add('input--error')
}

function showError(title: string, detail: string): void {
  errorTitle.textContent  = title
  errorDetail.textContent = detail
  errorArea.classList.remove('hidden')
  statusArea.classList.add('hidden')
  configCard.classList.remove('card--muted')
  scrollIntoView(errorArea)
}

function showResults(result: LoadTestResult, req: LoadTestRequest): void {
  resultsMeta.textContent =
    `url: ${req.url}\n` +
    `method: ${req.method}  ·  ` +
    `virtual users: ${req.virtualUsers}  ·  ` +
    `duration: ${req.durationSeconds}s`

  mTotal.textContent   = fmtInt(result.totalRequests)
  mSuccess.textContent = fmtInt(result.successfulRequests)
  mFailed.textContent  = fmtInt(result.failedRequests)

  mAvg.textContent = fmt(result.averageResponseTimeMs)
  mMin.textContent = fmt(result.minResponseTimeMs)
  mMax.textContent = fmt(result.maxResponseTimeMs)
  mP95.textContent = fmt(result.p95ResponseTimeMs)

  mRps.textContent       = fmt(result.requestsPerSecond, 1)
  mErrorRate.textContent = fmt(result.errorRate, 2)

  const hasFailed = result.failedRequests > 0
  mFailedCard.className   = 'metric-card' + (hasFailed ? ' metric-card--error' : ' metric-card--success')
  mErrorRateCard.className = 'metric-card' + (hasFailed ? ' metric-card--error' : ' metric-card--accent')

  resultsCard.classList.remove('hidden')
  configCard.classList.add('card--muted')
  statusArea.classList.add('hidden')
  scrollIntoView(resultsCard)
}

function buildErrorDetail(body: ApiErrorResponse): string {
  if (body.errors && Object.keys(body.errors).length > 0) {
    return Object.entries(body.errors)
      .map(([field, msg]) => `${field}: ${msg}`)
      .join('\n')
  }
  return body.message
}

function restoreLastUrl(): void {
  try {
    const saved = sessionStorage.getItem(URL_STORAGE_KEY)
    if (saved) urlInput.value = saved
  } catch {
    // sessionStorage unavailable — ignore
  }
}

function saveLastUrl(url: string): void {
  try {
    sessionStorage.setItem(URL_STORAGE_KEY, url)
  } catch {
    // sessionStorage unavailable — ignore
  }
}

// ── Help modal ────────────────────────────────────────────────────────────────

function openHelpModal(): void {
  helpTriggerElement = document.activeElement as HTMLElement | null
  helpModalBackdrop.classList.remove('hidden')
  helpModalBackdrop.setAttribute('aria-hidden', 'false')
  layout.classList.add('layout--blurred')
  document.body.classList.add('modal-open')
  helpModal.focus()
}

function closeHelpModal(): void {
  helpModalBackdrop.classList.add('hidden')
  helpModalBackdrop.setAttribute('aria-hidden', 'true')
  layout.classList.remove('layout--blurred')
  document.body.classList.remove('modal-open')
  helpTriggerElement?.focus()
  helpTriggerElement = null
}

function getFocusableModalElements(): HTMLElement[] {
  return Array.from(
    helpModal.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
    ),
  ).filter(el => !el.hasAttribute('disabled') && el.offsetParent !== null)
}

helpBtn.addEventListener('click', openHelpModal)
helpModalClose.addEventListener('click', closeHelpModal)

helpModalBackdrop.addEventListener('click', (e) => {
  if (e.target === helpModalBackdrop) closeHelpModal()
})

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && !helpModalBackdrop.classList.contains('hidden')) {
    closeHelpModal()
    return
  }

  if (e.key !== 'Tab' || helpModalBackdrop.classList.contains('hidden')) return

  const focusable = getFocusableModalElements()
  if (focusable.length === 0) return

  const first = focusable[0]
  const last = focusable[focusable.length - 1]

  if (e.shiftKey && document.activeElement === first) {
    e.preventDefault()
    last.focus()
  } else if (!e.shiftKey && document.activeElement === last) {
    e.preventDefault()
    first.focus()
  }
})

// ── Validation ────────────────────────────────────────────────────────────────

function clientValidate(): boolean {
  clearFieldErrors()

  const url      = urlInput.value.trim()
  const users    = parseInt(usersInput.value, 10)
  const duration = parseInt(durationInput.value, 10)
  let valid = true

  if (!url) {
    setFieldError('url', 'Target URL is required.')
    valid = false
  } else {
    const localhostPattern = /^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/.*)?$/
    if (!localhostPattern.test(url)) {
      setFieldError('url', 'URL must start with http://localhost or http://127.0.0.1.')
      valid = false
    }
  }

  if (isNaN(users) || users < 1 || users > 1000) {
    setFieldError('virtualUsers', 'Virtual users must be between 1 and 1,000.')
    valid = false
  }

  if (isNaN(duration) || duration < 1 || duration > 60) {
    setFieldError('durationSeconds', 'Duration must be between 1 and 60 seconds.')
    valid = false
  }

  if (!valid) {
    errorArea.classList.add('hidden')
    scrollIntoView(configCard)
  }

  return valid
}

// Clear inline error when user edits a field
for (const field of Object.keys(fieldInputs) as FieldName[]) {
  fieldInputs[field].addEventListener('input', () => {
    fieldErrors[field].classList.add('hidden')
    fieldInputs[field].classList.remove('input--error')
  })
}

// ── Submit handler ────────────────────────────────────────────────────────────

async function runLoadTest(): Promise<void> {
  if (!clientValidate()) return

  const request: LoadTestRequest = {
    url:             urlInput.value.trim(),
    method:          'GET',
    virtualUsers:    parseInt(usersInput.value, 10),
    durationSeconds: parseInt(durationInput.value, 10),
  }

  setRunning(true)
  showStatus()
  startCountdown(request.durationSeconds)
  scrollIntoView(statusArea)

  loadingPrimary.textContent = 'Running load test…'
  loadingSecondary.textContent =
    `${request.virtualUsers} virtual user${request.virtualUsers !== 1 ? 's' : ''} · ` +
    `${request.durationSeconds}s duration — backend runs the full duration before returning`

  try {
    const response = await fetch(`${API_BASE}/api/load-test`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })

    if (response.ok) {
      const result = (await response.json()) as LoadTestResult
      saveLastUrl(request.url)
      showResults(result, request)
    } else {
      let title   = `Error ${response.status}`
      let detail  = `The backend returned HTTP ${response.status}.`

      try {
        const body = (await response.json()) as ApiErrorResponse
        detail = buildErrorDetail(body)
        if (response.status === 400) title = 'Validation failed'
        else if (response.status === 500) title = 'Backend error'
      } catch {
        // body wasn't JSON — keep the generic message
      }

      showError(title, detail)
    }
  } catch (err) {
    const isNetworkError =
      err instanceof TypeError && err.message.toLowerCase().includes('fetch')

    if (isNetworkError) {
      const backendHint = API_BASE || 'http://localhost:8080'
      showError(
        'Cannot reach the backend',
        `Make sure the Spring Boot server is running at ${backendHint}.\n\n` +
        'From the backend/ directory: ./mvnw spring-boot:run',
      )
    } else {
      showError('Unexpected error', err instanceof Error ? err.message : String(err))
    }
  } finally {
    stopCountdown()
    setRunning(false)
    hideStatus()
  }
}

form.addEventListener('submit', (e) => {
  e.preventDefault()
  void runLoadTest()
})

runAgainBtn.addEventListener('click', () => {
  scrollIntoView(configCard)
  void runLoadTest()
})

restoreLastUrl()
