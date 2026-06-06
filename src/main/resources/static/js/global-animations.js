/* ==========================================================================
   MEDICORE HEALTH - GLOBAL ANIMATIONS ENGINE
   ========================================================================== */

document.addEventListener("DOMContentLoaded", () => {
    // 1. Khởi tạo thanh Scroll Progress Bar và Preloader trong DOM
    initGlobalElements();

    // 2. Xử lý tắt màn hình chào mừng (Preloader)
    handlePreloader();

    // 3. Xử lý Scroll Progress Tracker
    window.addEventListener("scroll", updateScrollProgress);

    // 4. Thiết lập Intersection Observer cho Scroll Reveal
    initScrollReveal();

    // 5. Tự động áp dụng Shimmer cho các button chính
    applyButtonEffects();
});

/**
 * Tự động tạo và chèn các thẻ HTML của Preloader và Scroll Progress Bar
 */
function initGlobalElements() {
    // Scroll Progress Bar
    if (!document.querySelector(".scroll-progress-container")) {
        const progressContainer = document.createElement("div");
        progressContainer.className = "scroll-progress-container";
        progressContainer.innerHTML = '<div class="scroll-progress-bar" id="progressBar"></div>';
        document.body.prepend(progressContainer);
    }

    // Preloader (Màn hình chào mừng)
    if (!document.querySelector(".preloader")) {
        const preloader = document.createElement("div");
        preloader.className = "preloader";
        preloader.id = "pagePreloader";
        preloader.innerHTML = `
            <div class="preloader-spinner"></div>
            <div class="preloader-text">MEDICORE HEALTH</div>
        `;
        document.body.prepend(preloader);
    }
}

/**
 * Tắt màn hình Preloader sau khi trang tải xong (hoặc tối đa 1.5 giây để tránh chờ lâu)
 */
function handlePreloader() {
    const preloader = document.getElementById("pagePreloader");
    if (!preloader) return;

    const fadeOutEffect = () => {
        preloader.classList.add("fade-out");
        setTimeout(() => {
            preloader.remove();
        }, 600);
    };

    // Tắt khi toàn bộ tài nguyên tải xong
    window.addEventListener("load", fadeOutEffect);

    // Backup: Tự động tắt sau 1.2s nếu tài nguyên ngoài quá chậm
    setTimeout(fadeOutEffect, 1200);
}

/**
 * Cập nhật độ rộng thanh tiến trình scroll
 */
function updateScrollProgress() {
    const progressBar = document.getElementById("progressBar");
    if (!progressBar) return;

    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const scrollPercentage = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0;

    progressBar.style.width = scrollPercentage + "%";
}

/**
 * Khởi tạo Intersection Observer để theo dõi và kích hoạt animation
 */
function initScrollReveal() {
    const revealElements = document.querySelectorAll(".reveal");

    // Tự động phân chia độ trễ (stagger delay) cho các nhóm phần tử xếp cạnh nhau
    const groups = {};
    revealElements.forEach((el) => {
        const parent = el.parentElement;
        // Lấy định danh duy nhất của thẻ cha
        const parentId = parent.id || parent.className || "default-group";
        
        // Nếu thẻ cha có class quy định stagger
        if (parent.classList.contains("stagger-container") || parent.classList.contains("service-box") || parent.classList.contains("stats") || parent.classList.contains("facility-grid")) {
            if (!groups[parentId]) {
                groups[parentId] = [];
            }
            groups[parentId].push(el);
        }
    });

    // Áp dụng transition-delay tăng dần cho các phần tử trong từng group stagger
    Object.keys(groups).forEach((key) => {
        groups[key].forEach((el, index) => {
            el.style.transitionDelay = `${index * 120}ms`;
        });
    });

    const revealObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add("visible");
                
                // Nếu phần tử chứa hiệu ứng chạy số liệu
                if (entry.target.classList.contains("count-up")) {
                    animateCounter(entry.target);
                }
                
                // Hủy theo dõi phần tử này vì nó đã hiện xong
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: "0px 0px -40px 0px"
    });

    revealElements.forEach((el) => revealObserver.observe(el));
}

/**
 * Hàm đếm số chuyển động (CountUp Effect)
 */
function animateCounter(element) {
    const target = parseFloat(element.getAttribute("data-target")) || 0;
    const duration = parseInt(element.getAttribute("data-duration")) || 1500; // ms
    const decimals = parseInt(element.getAttribute("data-decimals")) || 0;
    const suffix = element.getAttribute("data-suffix") || "";
    const prefix = element.getAttribute("data-prefix") || "";
    
    let startTime = null;

    function formatNumber(num) {
        if (decimals > 0) {
            return num.toFixed(decimals).replace(".", ",");
        }
        return Math.floor(num).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    }

    function step(timestamp) {
        if (!startTime) startTime = timestamp;
        const progress = Math.min((timestamp - startTime) / duration, 1);
        
        // Công thức Easing (easeOutQuad) để chạy chậm dần về cuối
        const easeProgress = progress * (2 - progress);
        const currentValue = easeProgress * target;
        
        element.textContent = prefix + formatNumber(currentValue) + suffix;
        
        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            element.textContent = prefix + formatNumber(target) + suffix;
        }
    }

    window.requestAnimationFrame(step);
}

/**
 * Tự động tìm và gán hiệu ứng phát sáng cho các nút bấm hành động chính
 */
function applyButtonEffects() {
    const buttons = document.querySelectorAll(".hero-box button, .btn-primary, .hero button, .login-btn, .nav-login-btn");
    buttons.forEach((btn) => {
        btn.classList.add("shimmer-btn");
    });
}
