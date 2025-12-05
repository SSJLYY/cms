import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ResourceDetail from '../ResourceDetail.vue'

// Mock API
vi.mock('../../api/resource', () => ({
  getResourceDetail: vi.fn(),
  recordDownload: vi.fn(),
  getLinkTypes: vi.fn()
}))

import { getResourceDetail, recordDownload, getLinkTypes } from '../../api/resource'

describe('ResourceDetail - 链接类型过滤', () => {
  let router
  
  const mockResource = {
    id: 1,
    title: '测试资源',
    description: '测试描述',
    downloadLinks: [
      { id: 1, linkType: 'quark', linkUrl: 'https://quark.com/test', linkName: '夸克网盘' },
      { id: 2, linkType: 'baidu', linkUrl: 'https://baidu.com/test', linkName: '百度网盘' },
      { id: 3, linkType: 'aliyun', linkUrl: 'https://aliyun.com/test', linkName: '阿里云盘' }
    ]
  }

  const mockLinkTypes = [
    { typeCode: 'quark', typeName: '夸克网盘' },
    { typeCode: 'baidu', typeName: '百度网盘' },
    { typeCode: 'aliyun', typeName: '阿里云盘' }
  ]

  beforeEach(() => {
    // 创建路由实例
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/resource/:id', component: ResourceDetail }
      ]
    })

    // Mock API 响应
    getResourceDetail.mockResolvedValue({ data: mockResource })
    getLinkTypes.mockResolvedValue({ data: mockLinkTypes })
    recordDownload.mockResolvedValue({})
  })

  it('应该在有type参数时只显示匹配的链接类型', async () => {
    // 导航到带有type参数的路由
    await router.push('/resource/1?type=quark')
    await router.isReady()

    const wrapper = mount(ResourceDetail, {
      global: {
        plugins: [router]
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证只显示夸克网盘链接
    const filteredLinks = wrapper.vm.filteredDownloadLinks
    expect(filteredLinks).toHaveLength(1)
    expect(filteredLinks[0].linkType).toBe('quark')
  })

  it('应该在没有type参数时显示所有链接', async () => {
    // 导航到不带type参数的路由
    await router.push('/resource/1')
    await router.isReady()

    const wrapper = mount(ResourceDetail, {
      global: {
        plugins: [router]
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证显示所有链接
    const filteredLinks = wrapper.vm.filteredDownloadLinks
    expect(filteredLinks).toHaveLength(3)
  })

  it('应该在type参数无效时显示所有链接', async () => {
    // 导航到带有无效type参数的路由
    await router.push('/resource/1?type=invalid')
    await router.isReady()

    const wrapper = mount(ResourceDetail, {
      global: {
        plugins: [router]
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证显示所有链接(因为没有匹配的)
    const filteredLinks = wrapper.vm.filteredDownloadLinks
    expect(filteredLinks).toHaveLength(3)
  })

  it('应该在资源没有指定类型链接时显示所有链接', async () => {
    // Mock 一个只有部分链接类型的资源
    const limitedResource = {
      ...mockResource,
      downloadLinks: [
        { id: 1, linkType: 'quark', linkUrl: 'https://quark.com/test', linkName: '夸克网盘' }
      ]
    }
    getResourceDetail.mockResolvedValue({ data: limitedResource })

    // 导航到请求不存在的链接类型
    await router.push('/resource/1?type=baidu')
    await router.isReady()

    const wrapper = mount(ResourceDetail, {
      global: {
        plugins: [router]
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证显示所有可用链接
    const filteredLinks = wrapper.vm.filteredDownloadLinks
    expect(filteredLinks).toHaveLength(1)
    expect(filteredLinks[0].linkType).toBe('quark')
  })

  it('应该正确记录下载操作', async () => {
    await router.push('/resource/1?type=quark')
    await router.isReady()

    const wrapper = mount(ResourceDetail, {
      global: {
        plugins: [router]
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 调用下载处理函数
    await wrapper.vm.handleDownload('quark')

    // 验证记录下载被调用
    expect(recordDownload).toHaveBeenCalledWith(1)
  })
})
