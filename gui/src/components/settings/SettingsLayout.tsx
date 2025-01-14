import { ReactNode, useEffect } from 'react';
import { Navbar } from '@/components/Navbar';
import { TopBar } from '@/components/TopBar';
import { SettingsSidebar } from './SettingsSidebar';
import { useBreakpoint } from '@/hooks/breakpoint';
import { Dropdown } from '@/components/commons/Dropdown';
import { useForm } from 'react-hook-form';
import { useLocalization } from '@fluent/react';
import { useLocation, useNavigate } from 'react-router-dom';
import './SettingsLayout.scss';

export function SettingSelectorMobile() {
  const { l10n } = useLocalization();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const links: { label: string; value: { url: string; scrollTo?: string } }[] =
    [
      {
        label: l10n.getString('settings-sidebar-general'),
        value: { url: '/settings/trackers', scrollTo: 'steamvr' },
      },
      {
        label: l10n.getString('settings-sidebar-interface'),
        value: { url: '/settings/interface', scrollTo: 'notifications' },
      },
      {
        label: l10n.getString('settings-sidebar-osc_router'),
        value: { url: '/settings/osc/router', scrollTo: 'router' },
      },
      {
        label: l10n.getString('settings-sidebar-osc_trackers'),
        value: { url: '/settings/osc/vrchat', scrollTo: 'vrchat' },
      },
      {
        label: 'VMC',
        value: { url: '/settings/osc/vmc', scrollTo: 'vmc' },
      },
      {
        label: l10n.getString('settings-sidebar-serial'),
        value: { url: '/settings/serial' },
      },
      {
        label: l10n.getString('settings-sidebar-firmware-tool'),
        value: { url: '/settings/firmware-tool' },
      },
      {
        label: l10n.getString('settings-sidebar-advanced'),
        value: { url: '/settings/advanced' },
      },
    ];

  const { control, watch, handleSubmit, setValue } = useForm<{
    link: string;
  }>({
    defaultValues: { link: links[0].value.url },
  });

  useEffect(() => {
    // This works because the component gets mounted/unmounted when switching beween desktop or mobile layout
    setValue('link', pathname, { shouldDirty: false, shouldTouch: false });
  }, []);

  useEffect(() => {
    const subscription = watch(() => handleSubmit(onSubmit)());
    return () => subscription.unsubscribe();
  }, []);

  const onSubmit = ({ link }: { link: string }) => {
    const item = links.find(({ value: { url } }) => url === link);

    if (!item) return;
    navigate(item.value.url, { state: { scrollTo: item.value.scrollTo } });
  };

  return (
    <div className="fixed top-12 z-50 px-4 w-full">
      <Dropdown
        control={control}
        display="block"
        items={links.map(({ label, value: { url: value } }) => ({
          label,
          value,
        }))}
        variant="tertiary"
        direction="down"
        // There is always an option selected placholder is not used
        placeholder=""
        name="link"
      ></Dropdown>
    </div>
  );
}

export function SettingsLayout({ children }: { children: ReactNode }) {
  const { isMobile } = useBreakpoint('mobile');
  return (
    <>
      <div className="settings-layout h-full">
        <div style={{ gridArea: 't' }}>
          <TopBar></TopBar>
        </div>
        <div style={{ gridArea: 'n' }}>
          <Navbar></Navbar>
        </div>
        <div style={{ gridArea: 's' }} className="my-2 mobile:hidden">
          <SettingsSidebar></SettingsSidebar>
        </div>
        <div
          style={{ gridArea: 'c' }}
          className="xs:pl-2 xs:pb-2 xs:mt-2 mobile:mt-7 overflow-y-auto"
        >
          {isMobile && <SettingSelectorMobile></SettingSelectorMobile>}
          {children}
        </div>
      </div>
    </>
  );
}
