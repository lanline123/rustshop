interface WebSocketMessage {
    accessToken: string;
    [key: string]: any;
}

class WebSocketService {
    private static instance: WebSocketService;
    private socket: WebSocket | null = null;
    private messageHandler: ((data: any) => void) | null = null;

    private constructor() {}

    public static getInstance(): WebSocketService {
        if (!WebSocketService.instance) {
            WebSocketService.instance = new WebSocketService();
        }
        return WebSocketService.instance;
    }

    public connect(url: string): void {
        if (this.socket) {
            return;
        }

        this.socket = new WebSocket(url);

        this.socket.onopen = () => {
            console.log("WebSocket connection opened.");
        };

        this.socket.onmessage = (event: MessageEvent) => {
            console.log("WebSocket message received:", event.data);
            if (this.messageHandler) {
                this.messageHandler(JSON.parse(event.data));
            }
        };

        this.socket.onclose = () => {
            console.log("WebSocket connection closed.");
            this.socket = null;
        };

        this.socket.onerror = (error: Event) => {
            console.error("WebSocket error:", error);
        };
    }

    public send(data: Omit<WebSocketMessage, "accessToken">): void {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            const message: WebSocketMessage = {
                accessToken: localStorage.getItem("accessToken") ?? "",
                ...data
            };
            this.socket.send(JSON.stringify(message));
            console.log("WebSocket send", message);
        }
    }
    public onMessage(handler: (data: any) => void): void {
        this.messageHandler = handler;
    }
    public close(): void {
        if (this.socket) {
            this.socket.close();
        }
    }
}

export default WebSocketService.getInstance();
