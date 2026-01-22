class PaymentGateway {
  constructor(options) {
    if (!options.key) {
      throw new Error('API key is required');
    }
    this.key = options.key;
    this.orderId = options.orderId;
    this.onSuccess = options.onSuccess || (() => {});
    this.onFailure = options.onFailure || (() => {});
    this.onClose = options.onClose || (() => {});
    this.modal = null;
    this.iframe = null;
  }

  open() {
    // Create modal overlay
    const modal = document.createElement('div');
    modal.id = 'payment-gateway-modal';
    modal.setAttribute('data-test-id', 'payment-modal');
    modal.style.position = 'fixed';
    modal.style.top = '0';
    modal.style.left = '0';
    modal.style.right = '0';
    modal.style.bottom = '0';
    modal.style.zIndex = '10000';
    modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';

    // Create modal content
    const modalContent = document.createElement('div');
    modalContent.className = 'modal-content';
    modalContent.style.position = 'relative';
    modalContent.style.width = '90%';
    modalContent.style.maxWidth = '600px';
    modalContent.style.height = '80vh';
    modalContent.style.backgroundColor = 'white';
    modalContent.style.borderRadius = '8px';
    modalContent.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
    modalContent.style.overflow = 'hidden';

    // Create close button
    const closeButton = document.createElement('button');
    closeButton.setAttribute('data-test-id', 'close-modal-button');
    closeButton.className = 'close-button';
    closeButton.innerHTML = '×';
    closeButton.style.position = 'absolute';
    closeButton.style.top = '10px';
    closeButton.style.right = '10px';
    closeButton.style.zIndex = '10001';
    closeButton.style.border = 'none';
    closeButton.style.background = 'none';
    closeButton.style.fontSize = '28px';
    closeButton.style.cursor = 'pointer';
    closeButton.onclick = () => this.close();

    // Create iframe
    const checkoutUrl = `http://localhost:3001/checkout?order_id=${this.orderId}&embedded=true`;
    const iframe = document.createElement('iframe');
    iframe.setAttribute('data-test-id', 'payment-iframe');
    iframe.src = checkoutUrl;
    iframe.style.width = '100%';
    iframe.style.height = '100%';
    iframe.style.border = 'none';

    modalContent.appendChild(iframe);
    modalContent.appendChild(closeButton);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    this.modal = modal;
    this.iframe = iframe;

    // Set up postMessage listener
    window.addEventListener('message', (event) => {
      if (event.origin !== 'http://localhost:3001') return;
      
      if (event.data.type === 'payment_success') {
        this.onSuccess(event.data.data);
        this.close();
      } else if (event.data.type === 'payment_failed') {
        this.onFailure(event.data.data);
      }
    });
  }

  close() {
    if (this.modal) {
      this.modal.remove();
      this.modal = null;
    }
    this.onClose();
  }
}

window.PaymentGateway = PaymentGateway;
