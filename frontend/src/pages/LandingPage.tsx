import { Logo } from '../components/AppShell'
import { ArrowIcon, CheckIcon, FileIcon, ShieldIcon, UploadIcon } from '../components/Icons'
import { navigate } from '../lib/route'

const benefits = [
  {
    icon: <UploadIcon />,
    title: 'Upload securely',
    text: 'Choose a musculoskeletal X-ray from your device. Your account keeps each image and result private to you.'
  },
  {
    icon: <FileIcon />,
    title: 'Review a structured result',
    text: 'See the identified fracture category, confidence context and a report you can take to a professional.'
  },
  {
    icon: <ShieldIcon />,
    title: 'Ask for plain language',
    text: 'If you choose, ask Groq AI to explain the result and suggest useful questions for a clinician.'
  }
]

export function LandingPage() {
  return (
    <main className="landing-page">
      <header className="landing-nav">
        <Logo />
        <div className="landing-nav__actions">
          <button className="text-button" type="button" onClick={() => navigate('/login')}>Sign in</button>
          <button className="primary-button compact" type="button" onClick={() => navigate('/register')}>Create account</button>
        </div>
      </header>

      <section className="landing-hero">
        <div className="landing-hero__content">
          <span className="eyebrow">AI-assisted fracture detection as a second opinion</span>
          <h1>An AI-Assisted Fracture Detection</h1>
          <p>FractureCare helps you review a musculoskeletal X-ray for possible fracture patterns, understand the result in straightforward language and prepare for a conversation with a qualified medical professional.</p>
          <div className="landing-actions">
            <button className="primary-button" type="button" onClick={() => navigate('/register')}>Get started <ArrowIcon /></button>
            <button className="secondary-button" type="button" onClick={() => navigate('/login')}>I already have an account</button>
          </div>
          <p className="landing-safety"><ShieldIcon /> For second opinions, not meant for emergency service.</p>
        </div>

        <aside className="landing-preview" aria-label="What a FractureCare review includes">
          <div className="landing-preview__top">
            <span className="eyebrow">Your review</span>
            <span className="landing-private"><ShieldIcon /> Private</span>
          </div>
          <h2>Useful as a Second Opinion</h2>
          <div className="landing-result-list">
            <div><span><CheckIcon /></span><p><strong>Possible fracture pattern</strong>A software-generated category for professional review</p></div>
            <div><span><CheckIcon /></span><p><strong>Confidence context</strong>How certain the system is about its classification</p></div>
            <div><span><CheckIcon /></span><p><strong>Optional AI explanation by Groq</strong>Plain-language help, provided only when you ask</p></div>
            <div><span><CheckIcon /></span><p><strong>Medical professional review</strong>Request a consented review from a registered medical professional</p></div>
          </div>
          <div className="landing-preview__note">Every result includes clear safety guidance and encourages qualified medical review.</div>
        </aside>
      </section>

      <section className="landing-benefits" aria-labelledby="benefits-heading">
        <div className="landing-section-heading">
          <span className="eyebrow">What FractureCare offers</span>
          <h2 id="benefits-heading">A second opinion designed to support your next step.</h2>
        </div>
        <div className="landing-benefit-grid">
          {benefits.map(benefit => (
            <article key={benefit.title}>
              <span className="landing-benefit-icon">{benefit.icon}</span>
              <h3>{benefit.title}</h3>
              <p>{benefit.text}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="landing-process" aria-labelledby="process-heading">
        <div className="landing-section-heading">
          <span className="eyebrow">How it works</span>
          <h2 id="process-heading">Three clear steps</h2>
        </div>
        <div className="landing-step-grid">
          <article><span>01</span><h3>Upload</h3><p>Add one musculoskeletal X-ray that you have permission to use.</p></article>
          <article><span>02</span><h3>Review</h3><p>Read the system's classification, confidence and safety information.</p></article>
          <article><span>03</span><h3>Discuss</h3><p>Use the result to support a conversation with a medical professional.</p></article>
        </div>
      </section>

      <section className="landing-boundary">
        <span className="landing-boundary__icon"><ShieldIcon /></span>
        <div><span className="eyebrow">Important</span><h2>Only for support</h2></div>
        <p>FractureCare cannot confirm or rule out a fracture, recommend treatment or handle emergencies. A qualified medical professional must interpret your X-ray alongside your symptoms and clinical history.</p>
      </section>

      <footer className="landing-footer">
        <Logo />
        <p>Educational second-opinion support for musculoskeletal X-rays.</p>
        <button className="text-button" type="button" onClick={() => navigate('/login')}>Sign in</button>
      </footer>
    </main>
  )
}

