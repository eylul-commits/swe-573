declare module 'element-plus' {
  export interface ElMessageOptions {
    message: string;
    type?: 'success' | 'warning' | 'info' | 'error';
    showClose?: boolean;
    duration?: number;
  }

  export function ElMessage(options: ElMessageOptions | string): void;

  const ElementPlus: any;
  export default ElementPlus;
}


