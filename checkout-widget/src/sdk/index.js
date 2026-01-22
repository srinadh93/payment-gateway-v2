import PaymentGateway from './PaymentGateway.js';
import './styles.css';

// Export for use as ES module
export default PaymentGateway;

// Also expose globally for direct script inclusion
if (typeof window !== 'undefined') {
  window.PaymentGateway = PaymentGateway;
}
